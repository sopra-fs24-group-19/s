package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.repository.TaskRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TaskPutDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.constant.TaskStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class TaskService {

    private final Logger log = LoggerFactory.getLogger(TaskService.class);
    private final UserService userService;
    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(
            @Qualifier("taskRepository") TaskRepository taskRepository,
                UserService userService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    public List<Task> getTasks() {
        return this.taskRepository.findAll();
    }

    public Task getTaskById(long id) {
        return this.taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + id));
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
        userService.subtractCoins(creator, newTask.getPrice());
        newTask = taskRepository.save(newTask);
        taskRepository.flush();
        return newTask;
    }

    @Transactional
    public void apply(TaskPutDTO taskPutDTO, String token) {
        User candidate = userService.getUserByToken(token);

        // Check for valid token and user
        if (candidate == null || token.isEmpty() || taskPutDTO.getUserId() != candidate.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token.");
        }

        long taskId = taskPutDTO.getTaskId();
        Task selectedTask = taskRepository.findById(taskId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "The selected task does not exist."));

        // Check if the candidate has already applied
        if (selectedTask.getCandidates().contains(candidate)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already applied.");
        }

        // Add the candidate to the task
        selectedTask.addCandidate(candidate);

        // Save the updated task
        taskRepository.save(selectedTask);
    }


    @Transactional
    public void selectCandidate(TaskPutDTO taskPutDTO, String token) {
        User helper = userService.getUserById(taskPutDTO.getHelperId());
        User taskCreator = userService.getUserById(taskPutDTO.getUserId());
        Task task = taskRepository.findById(taskPutDTO.getTaskId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "The task was not found."));

        // Check the taskCreator is performing the selection action
        if (!taskCreator.getToken().equals(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator of a task can choose the helper");
        }

        // Check if the helper is a valid candidate for the task
        if (!task.hasCandidate(helper)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "This helper has not applied for the job.");
        }

        // Set the helper and update the task status
        task.setHelper(helper);
        task.setStatus(TaskStatus.IN_PROGRESS);

        // Clear other applications
        clearOtherCandidates(task, helper);

        taskRepository.save(task); // Save the task with the new helper and status
    }

    public void clearOtherCandidates(Task task, User selectedHelper) {
        // Collect candidates to remove (all except the selected helper)
        List<User> candidatesToRemove = task.getCandidates().stream()
                .filter(candidate -> !candidate.equals(selectedHelper))
                .toList();

        // Update each candidate's applications to remove this task
        for (User candidate : candidatesToRemove) {
            candidate.getApplications().remove(task); // Remove the task from the candidate's applications
            userService.saveUser(candidate); // Persist changes to each candidate
        }

        // Clear candidates and re-add only the selected helper directly, if not already present
        task.getCandidates().removeIf(candidate -> !candidate.equals(selectedHelper));

        taskRepository.save(task); // Save changes to the task
    }


    @Transactional
    public void deleteTaskWithId(long taskId, String token) {
        Task taskToBeDeleted = taskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + taskId));

        User creator = taskToBeDeleted.getCreator();
        if (!checkPermissionToDeleteTask(token, creator.getId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Only the creator of this task is allowed to delete it");
        }

        // Manage the removal of candidates from the task
        for (User candidate : new ArrayList<>(taskToBeDeleted.getCandidates())) {
            candidate.getApplications().remove(taskToBeDeleted);
            userService.saveUser(candidate);
        }

        // Safely delete the task, as no foreign key constraints should block this operation
        taskRepository.delete(taskToBeDeleted);

        // Update the coins for the task creator
        creator.addCoins(taskToBeDeleted.getPrice());
        userService.saveUser(creator);
    }


    public Task confirmTask(long taskId, String token){
        Task taskToBeConfirmed = this.getTaskById(taskId);
        User creator = taskToBeConfirmed.getCreator();
        User helper = taskToBeConfirmed.getHelper();
        long currentUserId = userService.getUserIdByToken(token);

        if (currentUserId != creator.getId() && currentUserId != helper.getId()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only the creator or helper of this task are authorized to confirm it.");
        }

        if (currentUserId == creator.getId() && taskToBeConfirmed.getStatus() != TaskStatus.CONFIRMED_BY_CREATOR) {
            if (taskToBeConfirmed.getStatus() == TaskStatus.CONFIRMED_BY_HELPER) {
                taskToBeConfirmed.setStatus(TaskStatus.DONE);
                userService.addCoins(helper, taskToBeConfirmed.getPrice());
            } else {
                taskToBeConfirmed.setStatus(TaskStatus.CONFIRMED_BY_CREATOR);
            }
        } else if (currentUserId == helper.getId() && taskToBeConfirmed.getStatus() != TaskStatus.CONFIRMED_BY_HELPER) {
            if (taskToBeConfirmed.getStatus() == TaskStatus.CONFIRMED_BY_CREATOR) {
                taskToBeConfirmed.setStatus(TaskStatus.DONE);
                userService.addCoins(helper, taskToBeConfirmed.getPrice());
            } else {
                taskToBeConfirmed.setStatus(TaskStatus.CONFIRMED_BY_HELPER);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already confirmed this task.");
        }

        taskRepository.save(taskToBeConfirmed);
        return taskToBeConfirmed;
    }



    @Transactional
    public void deleteCandidate(long taskId, String token){
        Task task = this.getTaskById(taskId);
        User candidate = userService.getUserByToken(token);

        if (task.getCandidates().contains(candidate)) {
            task.getCandidates().remove(candidate);
            candidate.getApplications().remove(task);
            taskRepository.save(task);
            userService.saveUser(candidate);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The current user is not a candidate for this task");
        }
    }

    private boolean checkIfCreatorHasEnoughTokens(User creator, Task task) {
        return creator.getCoinBalance() >= task.getPrice();
    }

    private boolean checkPermissionToDeleteTask(String token, long creatorId) {
        long currentUserId = userService.getUserIdByToken(token);
        return currentUserId == creatorId;
    }

    /*
    public void deleteApplicationsByTask(Task task, User helper){
        long helperId= helper.getId();
        List<Application> applicationList = applicationsRepository.findApplicationsByTaskIdExcludingHelperId(task.getId(), helperId);
        for (Application application : applicationList) {
            applicationsRepository.delete(application);
        }
    }
     */
}

