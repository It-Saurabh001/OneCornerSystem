package com.saurabh.onecornersystem.domain.usecase.chat

import com.saurabh.onecornersystem.data.model.Message
import com.saurabh.onecornersystem.data.repository.ChatRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for sending a message in chat
 */
class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) : BaseUseCase {

    fun execute(
        chatId: String,
        message: Message
    ): Flow<Resource<Message>> {
        if (chatId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Chat ID cannot be empty"))
            }
        }

        if (message.messageId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Message ID cannot be empty"))
            }
        }

        if (message.text.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Message content cannot be empty"))
            }
        }

        return chatRepository.sendMessage(chatId, message)
    }
}

