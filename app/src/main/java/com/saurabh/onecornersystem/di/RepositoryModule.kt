package com.saurabh.onecornersystem.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.saurabh.onecornersystem.data.repository.ShopRepository
import com.saurabh.onecornersystem.domain.repository.ShopRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideShopRepository(firestore: FirebaseFirestore,
                              @ApplicationContext context: Context): ShopRepository {
        return ShopRepositoryImpl(
            firestore,
            contentResolver = context.contentResolver
        )
    }

//    @Provides
//    @Singleton
//    fun provideProductRepository(firestore: FirebaseFirestore): ProductRepository {
//        return ProductRepositoryImpl(firestore)
//    }
//
//    @Provides
//    @Singleton
//    fun provideOrderRepository(firestore: FirebaseFirestore): OrderRepository {
//        return OrderRepositoryImpl(firestore)
//    }
//
//    @Provides
//    @Singleton
//    fun provideChatRepository(firestore: FirebaseFirestore): ChatRepository {
//        return ChatRepositoryImpl(firestore)
//    }
//
//    @Provides
//    @Singleton
//    fun provideInventoryRepository(firestore: FirebaseFirestore): InventoryRepository {
//        return InventoryRepositoryImpl(firestore)
//    }
//
//    @Provides
//    @Singleton
//    fun provideAnalyticsRepository(firestore: FirebaseFirestore): AnalyticsRepository {
//        return AnalyticsRepositoryImpl(firestore)
//    }
}

