package com.saurabh.onecornersystem.di

import com.google.firebase.firestore.FirebaseFirestore
import com.saurabh.onecornersystem.data.repository.ShopRepository
import com.saurabh.onecornersystem.data.repository.ProductRepository
import com.saurabh.onecornersystem.data.repository.OrderRepository
import com.saurabh.onecornersystem.data.repository.ChatRepository
import com.saurabh.onecornersystem.data.repository.InventoryRepository
import com.saurabh.onecornersystem.data.repository.AnalyticsRepository
import com.saurabh.onecornersystem.domain.repository.ShopRepositoryImpl
import com.saurabh.onecornersystem.domain.repository.ProductRepositoryImpl
import com.saurabh.onecornersystem.domain.repository.OrderRepositoryImpl
import com.saurabh.onecornersystem.domain.repository.ChatRepositoryImpl
import com.saurabh.onecornersystem.domain.repository.InventoryRepositoryImpl
import com.saurabh.onecornersystem.domain.repository.AnalyticsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideShopRepository(firestore: FirebaseFirestore): ShopRepository {
        return ShopRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideProductRepository(firestore: FirebaseFirestore): ProductRepository {
        return ProductRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideOrderRepository(firestore: FirebaseFirestore): OrderRepository {
        return OrderRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideChatRepository(firestore: FirebaseFirestore): ChatRepository {
        return ChatRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideInventoryRepository(firestore: FirebaseFirestore): InventoryRepository {
        return InventoryRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideAnalyticsRepository(firestore: FirebaseFirestore): AnalyticsRepository {
        return AnalyticsRepositoryImpl(firestore)
    }
}

