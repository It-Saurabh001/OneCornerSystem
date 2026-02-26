package com.saurabh.onecornersystem.domain.usecase.chat

import com.saurabh.onecornersystem.data.repository.ChatRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for marking chat messages as read
 */
class MarkAsReadUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) : BaseUseCase {

    fun execute(chatId: String): Flow<Resource<Boolean>> {
        if (chatId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Chat ID cannot be empty"))
            }
        }

        return chatRepository.markAllMessagesAsRead(chatId)
    }
}

