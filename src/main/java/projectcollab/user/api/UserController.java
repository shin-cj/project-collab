package projectcollab.user.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import projectcollab.user.application.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserCreateRequest request){
        return userService.create(request);
    }

    @GetMapping("/{userId}")
    public UserResponse findById(@PathVariable Long userId){
        return userService.findById(userId);
    }

    @GetMapping
    public List<UserResponse> findAll(){
        return userService.findAll();
    }
}
