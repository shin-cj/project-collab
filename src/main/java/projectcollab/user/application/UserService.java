package projectcollab.user.application;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import projectcollab.user.api.UserCreateRequest;
import projectcollab.user.api.UserResponse;
import projectcollab.user.domain.User;
import projectcollab.user.infrastructure.UserRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse create(UserCreateRequest request){
        if(userRepository.existsByEmail(request.email())){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"이미 사용 중인 이메일입니다.");
        }

        User user = new User(request.name(),request.email());

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    public UserResponse findById(Long userId){
        User user = userRepository.findById(userId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"사용자를 찾을 수 없습니다."));

        return UserResponse.from(user);
    }

    public List<UserResponse> findAll(){
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

}
