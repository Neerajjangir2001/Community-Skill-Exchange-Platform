package com.chat_service.repository;

import com.chat_service.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    // Find conversation between two users
    @Query("{ 'participants': { $all: [?0, ?1], $size: 2 }} ")
    List<Conversation> findByParticipants(String user1, String user2);

    List<Conversation> findByParticipantsContainingOrderByLastMessageTimeDesc(String userId);

    // Find active (non-archived) conversations
    @Query("{ 'participants': ?0, 'archived': false }")
    List<Conversation> findActiveConversationsByUserId(String userId);
}
