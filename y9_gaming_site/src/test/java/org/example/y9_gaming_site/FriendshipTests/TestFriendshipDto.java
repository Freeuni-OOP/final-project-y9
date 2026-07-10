package org.example.y9_gaming_site.FriendshipTests;

import junit.framework.TestCase;
import org.example.y9_gaming_site.friendship.AcceptDto;
import org.example.y9_gaming_site.friendship.RequestDto;

public class TestFriendshipDto extends TestCase {

    public void testRequestDto() {
        RequestDto dto = new RequestDto();
        dto.setSenderId(10L);
        dto.setReceiverId(20L);

        assertEquals(Long.valueOf(10L), dto.getSenderId());
        assertEquals(Long.valueOf(20L), dto.getReceiverId());
    }

    public void testAcceptDto() {
        AcceptDto dto = new AcceptDto();
        dto.setFriendshipId(500L);

        assertEquals(500L, dto.getFriendshipId());
    }
}
