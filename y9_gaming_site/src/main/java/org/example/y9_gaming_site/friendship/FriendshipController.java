package org.example.y9_gaming_site.friendship;

import org.example.y9_gaming_site.friendship.Friendship;
import org.example.y9_gaming_site.friendship.AcceptDto;
import org.example.y9_gaming_site.friendship.Friendship;
import org.example.y9_gaming_site.friendship.FriendshipService;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/friends")
public class FriendshipController {
    private FriendshipService service;
    public FriendshipController(FriendshipService service) {
        this.service = service;
    }


    @PostMapping("/request")
    public Friendship sendRequest(@RequestBody RequestDto dto) {
        return service.sendRequest(dto.getSenderId(),  dto.getReceiverId());
    }


    @PostMapping("/accept")
    public Friendship acceptRequest(@RequestBody AcceptDto dto) {
        return service.acceptRequest(dto.getFriendshipId());
    }


    @GetMapping("/pending/{userId}")
    public List<Friendship> getPendingRequests(@PathVariable("userId") Long userId) {
        return service.getPendingRequests(userId);
    }

    @GetMapping("/status")
    public String getStatus(@RequestParam Long myId, @RequestParam Long otherId) {
        return service.getStatus(myId, otherId);
    }
}


