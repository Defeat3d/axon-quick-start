package io.axoniq.labs.chat.commandmodel;

import io.axoniq.labs.chat.coreapi.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.ArrayList;
import java.util.List;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class ChatRoom {

    @AggregateIdentifier
    private String roomId;

    private List<String> participants = new ArrayList<>();

    public ChatRoom() {

    }

    @CommandHandler
    public ChatRoom(CreateRoomCommand command) {
        apply(new RoomCreatedEvent(command.getRoomId(), command.getName()));
    }

    @CommandHandler
    public void handle(JoinRoomCommand command) {
        if (!participants.contains(command.getParticipant())) {
            apply(new ParticipantJoinedRoomEvent(command.getParticipant(), command.getRoomId()));
        }
    }

    @CommandHandler
    public void handle(LeaveRoomCommand command) {
        if (participants.contains(command.getParticipant())) {
            apply(new ParticipantLeftRoomEvent(command.getParticipant(), command.getRoomId()));
        }
    }

    @CommandHandler
    public void handle(PostMessageCommand command) {
        if (!participants.contains(command.getParticipant())) {
            throw new IllegalStateException(String.format("Participant %s is not in chatroom %s", command.getParticipant(), command.getRoomId()));
        }
        apply(new MessagePostedEvent(command.getParticipant(), command.getRoomId(), command.getMessage()));
    }

    @EventSourcingHandler
    public void on(RoomCreatedEvent event) {
        this.roomId = event.getRoomId();
    }

    @EventSourcingHandler
    public void on(ParticipantJoinedRoomEvent event) {
        participants.add(event.getParticipant());
    }

    @EventSourcingHandler
    public void on(ParticipantLeftRoomEvent event) {
        participants.remove(event.getParticipant());
    }

}
