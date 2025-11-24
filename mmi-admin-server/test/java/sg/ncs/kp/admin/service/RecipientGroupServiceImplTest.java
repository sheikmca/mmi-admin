package sg.ncs.kp.admin.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import sg.ncs.kp.admin.BaseTest;
import sg.ncs.kp.admin.dto.AssignUserDTO;
import sg.ncs.kp.admin.dto.RemoveUserFromGroupDTO;
import sg.ncs.kp.admin.mapper.RecipientGroupMapper;
import sg.ncs.kp.admin.mapper.UserRecipientGroupMappingMapper;
import sg.ncs.kp.admin.po.RecipientGroup;
import sg.ncs.kp.admin.service.impl.RecipientGroupServiceImpl;
import sg.ncs.kp.common.exception.pojo.ClientServiceException;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.UserGroupAssignUserDTO;

import java.util.ArrayList;

/**
 * @author Wang Shujin
 * @date 2022/8/29 9:57
 */
public class RecipientGroupServiceImplTest extends BaseTest {

    @Mock
    private RecipientGroupMapper recipientGroupMapper;

    @Mock
    private UserRecipientGroupMappingMapper userRecipientGroupMappingMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private RecipientGroupServiceImpl recipientGroupService;

    @Test
    void insertTest() {
        Assertions.assertDoesNotThrow(() -> recipientGroupService.insert(new RecipientGroup()));
    }

    @Test
    void update() {
        Mockito.when(recipientGroupMapper.selectById(1)).thenReturn(null);
        Mockito.when(recipientGroupMapper.selectById(2)).thenReturn(new RecipientGroup(){{setTenantId("1");}});
        try (MockedStatic<SessionUtil> mockedStatic = Mockito.mockStatic(SessionUtil.class)) {
            mockedStatic.when(SessionUtil::getTenantId).thenReturn("1");
            Assertions.assertThrows(ClientServiceException.class, () -> recipientGroupService.update(new RecipientGroup(){{setId(1);}}));
            Assertions.assertDoesNotThrow(() -> recipientGroupService.update(new RecipientGroup(){{setId(2);}}));
        }
    }

    @Test
    void deleteTest() {
        HashOperations hashOperations = Mockito.mock(HashOperations.class);
        Mockito.when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        Assertions.assertDoesNotThrow(() -> recipientGroupService.delete(1));
    }

    @Test
    void selectAllTest() {
        Assertions.assertDoesNotThrow(() -> recipientGroupService.selectAll("",""));
    }

    @Test
    void assignedUserListTest() {
        Assertions.assertDoesNotThrow(() -> recipientGroupService.assignedUserList(1, new UserGroupAssignUserDTO(){{setPageNo(1);setPageSize(1);}}));
    }

    @Test
    void groupNotAssignedUserList() {
        Assertions.assertDoesNotThrow(() -> recipientGroupService.groupNotAssignedUserList(1, new UserGroupAssignUserDTO(){{setPageNo(1);setPageSize(1);}}));
    }

    @Test
    void assignUserToRecipientGroupTest() {
        try (MockedStatic<SessionUtil> mockedStatic = Mockito.mockStatic(SessionUtil.class)) {
            HashOperations hashOperations = Mockito.mock(HashOperations.class);
            Mockito.when(redisTemplate.opsForHash()).thenReturn(hashOperations);
            mockedStatic.when(SessionUtil::getTenantId).thenReturn("1");
            Mockito.when(recipientGroupMapper.selectById(1)).thenReturn(new RecipientGroup(){{setTenantId("2");}});
            Mockito.when(recipientGroupMapper.selectById(2)).thenReturn(new RecipientGroup(){{setTenantId("1");}});
            Mockito.when(userRecipientGroupMappingMapper.selectCount(Mockito.any())).thenReturn(0L);
            AssignUserDTO dto = new AssignUserDTO();
            Assertions.assertDoesNotThrow(() -> recipientGroupService.assignUserToRecipientGroup(dto));
            dto.setGroupId(1);
            dto.setUserIds(new ArrayList<>(){{add("1");add("2");}});
            Assertions.assertThrows(ClientServiceException.class, () -> recipientGroupService.assignUserToRecipientGroup(dto));
            dto.setGroupId(2);
            Assertions.assertDoesNotThrow(() -> recipientGroupService.assignUserToRecipientGroup(dto));
        }
    }

    @Test
    void removeUserTest() {
        HashOperations hashOperations = Mockito.mock(HashOperations.class);
        Mockito.when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        RemoveUserFromGroupDTO dto = new RemoveUserFromGroupDTO();
        dto.setGroupId(1);
        dto.setUserIds(new ArrayList<>(){{add("1");add("2");}});
        Assertions.assertDoesNotThrow(() -> recipientGroupService.removeUser(dto));
    }

}
