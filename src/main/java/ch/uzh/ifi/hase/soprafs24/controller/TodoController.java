package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.TaskService;
import ch.uzh.ifi.hase.soprafs24.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
public class TodoController {

    private final TodoService todoService;

    TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping("/todo")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public void createTodo(@RequestBody TodoPostDTO todoPostDTO, @RequestHeader("Authorization") String token) {
        Todo todoInput = DTOMapper.INSTANCE.convertTodoPostDTOToEntity(todoPostDTO);
        long taskId = todoPostDTO.getTaskId();

        todoService.createTodo(todoInput,taskId,token);
    }

    @PutMapping("/todo/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void updateTodo(@RequestBody TodoPutDTO todoPutDTO, @RequestHeader("Authorization") String token, @PathVariable("id") long id) {
        Todo todoInput = DTOMapper.INSTANCE.convertTodoPutDTOToEntity(todoPutDTO);
        //long taskId = todoPostDTO.getTaskId();
        todoService.updateTodo(todoInput,token, id);
    }
}
