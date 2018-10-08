package io.axoniq.labs.chat.query.rooms.messages;

import io.axoniq.labs.chat.coreapi.MessagePostedEvent;
import io.axoniq.labs.chat.coreapi.RoomMessagesQuery;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;


@Component
public class ChatMessageProjection {

    private final ChatMessageRepository repository;

    private final QueryUpdateEmitter updateEmitter;

    public ChatMessageProjection(ChatMessageRepository repository,
                                 QueryUpdateEmitter updateEmitter) {
        this.repository = repository;
        this.updateEmitter = updateEmitter;
    }

    @EventHandler
    public void on(MessagePostedEvent event, @Timestamp Instant timestamp) {
        repository.save(new ChatMessage(event.getParticipant(), event.getRoomId(), event.getMessage(), timestamp.toEpochMilli()));
        updateEmitter.emit(RoomMessagesQuery.class, query -> query.getRoomId().equals(event.getRoomId()), event.getMessage());
    }

    @QueryHandler
    public List<ChatMessage> findAllChatMessagesByRoomId(RoomMessagesQuery query) {
        return repository.findAllByRoomIdOrderByTimestamp(query.getRoomId());
    }

    // TODO: Emit updates when new message arrive to notify subscription query by modifying the event handler

}
