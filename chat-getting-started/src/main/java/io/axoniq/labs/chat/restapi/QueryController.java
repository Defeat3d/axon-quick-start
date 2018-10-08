package io.axoniq.labs.chat.restapi;

import io.axoniq.labs.chat.coreapi.AllRoomsQuery;
import io.axoniq.labs.chat.coreapi.RoomMessagesQuery;
import io.axoniq.labs.chat.coreapi.RoomParticipantsQuery;
import io.axoniq.labs.chat.query.rooms.messages.ChatMessage;
import io.axoniq.labs.chat.query.rooms.summary.RoomSummary;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.Future;

@RestController
public class QueryController {

    private final QueryGateway gateway;

    public QueryController(QueryGateway gateway) {
        this.gateway = gateway;
    }

    @GetMapping("rooms")
    public Future<List<RoomSummary>> listRooms() {
        return gateway.query(new AllRoomsQuery(), ResponseTypes.multipleInstancesOf(RoomSummary.class));
    }

    @GetMapping("/rooms/{roomId}/participants")
    public Future<List<String>> participantsInRoom(@PathVariable String roomId) {
        return gateway.query(new RoomParticipantsQuery(roomId), ResponseTypes.multipleInstancesOf(String.class));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public Future<List<ChatMessage>> roomMessages(@PathVariable String roomId) {
        return gateway.query(new RoomMessagesQuery(roomId), ResponseTypes.multipleInstancesOf(ChatMessage.class));
    }

    @GetMapping(value = "/rooms/{roomId}/messages/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatMessage> subscribeRoomMessages(@PathVariable String roomId) {
        return gateway.subscriptionQuery(new RoomMessagesQuery(roomId), ResponseTypes.multipleInstancesOf(ChatMessage.class), ResponseTypes.instanceOf(ChatMessage.class)).updates();
    }
}
