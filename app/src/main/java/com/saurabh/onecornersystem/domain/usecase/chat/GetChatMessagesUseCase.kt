package com.saurabh.onecornersystem.domain.usecase.chat

import com.saurabh.onecornersystem.data.model.Message
import com.saurabh.onecornersystem.data.repository.ChatRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for fetching chat messages
 */
class GetChatMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) : BaseUseCase {

    fun execute(chatId: String, limit: Int = 50): Flow<Resource<List<Message>>> {
        if (chatId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Chat ID cannot be empty"))
            }
        }

        if (limit <= 0) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Limit must be greater than 0"))
            }
        }

        return chatRepository.getChatMessages(chatId, limit)
    }
}


