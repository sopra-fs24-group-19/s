package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.repository.ApplicationsRepository;
import ch.uzh.ifi.hase.soprafs24.repository.TaskRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TaskPutDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Application;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
//import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.constant.TaskStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class TaskService {

    private final Logger log = LoggerFactory.getLogger(TaskService.class);
    private final UserService userService;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ApplicationsRepository applicationsRepository;

    @Autowired
    public TaskService(
            @Qualifier("taskRepository") TaskRepository taskRepository,
            ApplicationsRepository applicationsRepository,
            UserRepository userRepository,
                UserService userService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.applicationsRepository= applicationsRepository;
    }

    public List<Task> getTasks() {
        return this.taskRepository.findAll();
    }

    public List<Task> getTasksByCreator(long userId) {
        return this.taskRepository.findByCreatorId(userId);
    }

    public List<User> getCandidatesForTaskWithId(long taskId) {
        return userService.getCandidatesByTaskId(taskId);
    }

    public List<Task> getTasksByApplicant(long userId) {
        return this.taskRepository.findTasksByApplicantId(userId);
    }

    public Task createTask(Task newTask, long userId) {
        User creator = userService.getUserById(userId);
        boolean valid = checkIfCreatorHasEnoughTokens(creator, newTask);
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "creator does not have enough credits");
        }
        newTask.setCreator(creator);
        newTask.setStatus(TaskStatus.CREATED);
        newTask = taskRepository.save(newTask);
        taskRepository.flush();
        userService.subtractCoins(creator, newTask.getPrice());
        log.debug("Created task: {}", newTask);
        return newTask;
    }

    public void apply(TaskPutDTO taskPutDTO, String token){
        User candidate = userRepository.findUserByToken(token);
        //to check if there is a token or the token has been manipulated
        if (candidate==null || token.isEmpty() || taskPutDTO.getUserId()!=candidate.getId()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token");
        }
        long taskId= taskPutDTO.getTaskId();
        Application newApplication = new Application();
        Task selectedTask = taskRepository.findById(taskId);
        newApplication.setTask(selectedTask);
        newApplication.setUser(candidate);
        applicationsRepository.saveAndFlush(newApplication);
    }

    public void deleteTaskWithId(long taskId, String token) {
        Task taskToBeDeleted = taskRepository.findById(taskId);
        if (taskToBeDeleted == null) {
            throw new NoSuchElementException("Task not found with id: " + taskId);
        }
        if (!checkPermissionToDeleteTask(token, taskToBeDeleted.getCreator().getId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "only the creator of this task is allowed to delete it");
        }
        taskRepository.delete(taskToBeDeleted);
    }

    private boolean checkIfCreatorHasEnoughTokens(User creator, Task task) {
        return creator.getCoinBalance() >= task.getPrice();
    }

    private boolean checkPermissionToDeleteTask(String token, long creatorId) {
        long currentUserId = userService.getUserIdByToken(token);
        return currentUserId == creatorId;
    }
}
