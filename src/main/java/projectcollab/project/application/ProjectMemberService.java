package projectcollab.project.application;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import projectcollab.project.api.ProjectMemberAddRequest;
import projectcollab.project.api.ProjectMemberResponse;
import projectcollab.project.api.ProjectMemberRoleUpdateRequest;
import projectcollab.project.api.ProjectResponse;
import projectcollab.project.domain.ProjectMember;
import projectcollab.project.domain.Role;
import projectcollab.project.infrastructure.ProjectMemberRepository;
import projectcollab.user.domain.User;
import projectcollab.user.infrastructure.UserRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public ProjectMemberService(ProjectMemberRepository projectMemberRepository,UserRepository userRepository){
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    public List<ProjectMemberResponse> findAll(Long projectId,Long requesterId){
        findMembership(projectId,requesterId);

        return projectMemberRepository.findAllByProjectId(projectId).stream().map(ProjectMemberResponse::from).toList();
    }

    @Transactional
    public ProjectMemberResponse add(Long projectId, Long requestId, ProjectMemberAddRequest request){
        ProjectMember requesterMemberShip = findMembership(projectId,requestId);

        requireOwnerOrAdmin(requesterMemberShip);

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.userId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"이미 프로젝트에 속한 사용자입니다.");
        }

        User targetUser = userRepository.findById(request.userId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"추가할 사용자를 찾을 수 없습니다."));

        ProjectMember newMember = new ProjectMember(requesterMemberShip.getProject(),targetUser, request.role());

        ProjectMember savedMember = projectMemberRepository.save(newMember);

        return ProjectMemberResponse.from(savedMember);
    }

    @Transactional
    public ProjectMemberResponse changeRole(Long projectId, Long memberId, Long requesterId, ProjectMemberRoleUpdateRequest request){

        ProjectMember requestMemberShip = findMembership(projectId,requesterId);

        requireOwnerOrAdmin(requestMemberShip);

        ProjectMember targetMember = findProjectMember(projectId,memberId);

        protectLastOwner(projectId,targetMember,request.role());
        targetMember.changeRole(request.role());

        return ProjectMemberResponse.from(targetMember);

    }

    @Transactional
    public void remove(Long projectId,Long memberId, Long requesterId){
        ProjectMember requesterMembership = findMembership(projectId,requesterId);

        requireOwnerOrAdmin(requesterMembership);

        ProjectMember targetMember = findProjectMember(projectId,memberId);

        if(targetMember.getRole() == Role.OWNER){
            long ownerCount = projectMemberRepository.countByProjectIdAndRole(projectId,Role.OWNER);

            if(ownerCount<=1){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"프로젝트에는 최소 1명의 오너가 필요합니다.");
            }
        }

        projectMemberRepository.delete(targetMember);
    }

    private ProjectMember findMembership(
            Long projectId,
            Long userId
    ) {
        return projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "프로젝트에 접근할 권한이 없습니다."
                ));
    }

    private ProjectMember findProjectMember(Long projectId,Long memberId){
        return projectMemberRepository.findByIdAndProjectId(memberId,projectId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"프로젝트 멤버를 찾을 수 없습니다."));
    }

    private void requireOwnerOrAdmin(ProjectMember membership){
        Role role = membership.getRole();

        if(role != Role.OWNER && role != Role.ADMIN){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"OWNER 또는 ADMIN 권한이 필요합니다.");
        }
    }

    private void protectLastOwner(Long projectId,ProjectMember targetMember,Role newRole){
        if(targetMember.getRole() == Role.OWNER && newRole != Role.OWNER){
            long ownerCount = projectMemberRepository.countByProjectIdAndRole(projectId,Role.OWNER);

            if(ownerCount<=1){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"마지막 OWNER의 역할은 변경할 수 없습니다.");
            }
        }
    }

}
