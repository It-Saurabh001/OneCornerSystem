package com.saurabh.onecornersystem.di

import com.saurabh.onecornersystem.data.repository.AnalyticsRepository
import com.saurabh.onecornersystem.data.repository.ChatRepository
import com.saurabh.onecornersystem.data.repository.InventoryRepository
import com.saurabh.onecornersystem.data.repository.OrderRepository
import com.saurabh.onecornersystem.data.repository.ProductRepository
import com.saurabh.onecornersystem.data.repository.ShopRepository
import com.saurabh.onecornersystem.domain.usecase.analytics.GenerateReportUseCase
import com.saurabh.onecornersystem.domain.usecase.analytics.GetShopAnalyticsUseCase
import com.saurabh.onecornersystem.domain.usecase.analytics.GetTopProductsUseCase
import com.saurabh.onecornersystem.domain.usecase.chat.GetChatMessagesUseCase
import com.saurabh.onecornersystem.domain.usecase.chat.MarkAsReadUseCase
import com.saurabh.onecornersystem.domain.usecase.chat.SendMessageUseCase
import com.saurabh.onecornersystem.domain.usecase.inventory.CheckLowStockUseCase
import com.saurabh.onecornersystem.domain.usecase.inventory.SyncInventoryUseCase
import com.saurabh.onecornersystem.domain.usecase.inventory.UpdateStockUseCase
import com.saurabh.onecornersystem.domain.usecase.order.AcceptOrderUseCase
import com.saurabh.onecornersystem.domain.usecase.order.GetShopOrdersUseCase
import com.saurabh.onecornersystem.domain.usecase.order.MarkOrderAsDeliveredUseCase
import com.saurabh.onecornersystem.domain.usecase.order.RejectOrderUseCase
import com.saurabh.onecornersystem.domain.usecase.order.UpdateOrderStatusUseCase
import com.saurabh.onecornersystem.domain.usecase.product.AddProductVariantUseCase
import com.saurabh.onecornersystem.domain.usecase.product.CreateProductUseCase
import com.saurabh.onecornersystem.domain.usecase.product.DeleteProductUseCase
import com.saurabh.onecornersystem.domain.usecase.product.ListProductsUseCase
import com.saurabh.onecornersystem.domain.usecase.product.UpdatePricingUseCase
import com.saurabh.onecornersystem.domain.usecase.product.UpdateProductUseCase
import com.saurabh.onecornersystem.domain.usecase.shop.CreateShopUseCase
import com.saurabh.onecornersystem.domain.usecase.shop.DeactivateShopUseCase
import com.saurabh.onecornersystem.domain.usecase.shop.GetShopDetailsUseCase
import com.saurabh.onecornersystem.domain.usecase.shop.UpdateShopProfileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing use case instances
 * All use cases are singleton scoped
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // Shop Use Cases
    @Provides
    @Singleton
    fun provideCreateShopUseCase(shopRepository: ShopRepository): CreateShopUseCase {
        return CreateShopUseCase(shopRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateShopProfileUseCase(shopRepository: ShopRepository): UpdateShopProfileUseCase {
        return UpdateShopProfileUseCase(shopRepository)
    }

    @Provides
    @Singleton
    fun provideGetShopDetailsUseCase(shopRepository: ShopRepository): GetShopDetailsUseCase {
        return GetShopDetailsUseCase(shopRepository)
    }

    @Provides
    @Singleton
    fun provideDeactivateShopUseCase(shopRepository: ShopRepository): DeactivateShopUseCase {
        return DeactivateShopUseCase(shopRepository)
    }

    // Product Use Cases
    @Provides
    @Singleton
    fun provideCreateProductUseCase(productRepository: ProductRepository): CreateProductUseCase {
        return CreateProductUseCase(productRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateProductUseCase(productRepository: ProductRepository): UpdateProductUseCase {
        return UpdateProductUseCase(productRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteProductUseCase(productRepository: ProductRepository): DeleteProductUseCase {
        return DeleteProductUseCase(productRepository)
    }

    @Provides
    @Singleton
    fun provideListProductsUseCase(productRepository: ProductRepository): ListProductsUseCase {
        return ListProductsUseCase(productRepository)
    }

    @Provides
    @Singleton
    fun provideAddProductVariantUseCase(productRepository: ProductRepository): AddProductVariantUseCase {
        return AddProductVariantUseCase(productRepository)
    }

    @Provides
    @Singleton
    fun provideUpdatePricingUseCase(productRepository: ProductRepository): UpdatePricingUseCase {
        return UpdatePricingUseCase(productRepository)
    }

    // Order Use Cases
    @Provides
    @Singleton
    fun provideAcceptOrderUseCase(orderRepository: OrderRepository): AcceptOrderUseCase {
        return AcceptOrderUseCase(orderRepository)
    }

    @Provides
    @Singleton
    fun provideRejectOrderUseCase(orderRepository: OrderRepository): RejectOrderUseCase {
        return RejectOrderUseCase(orderRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateOrderStatusUseCase(orderRepository: OrderRepository): UpdateOrderStatusUseCase {
        return UpdateOrderStatusUseCase(orderRepository)
    }

    @Provides
    @Singleton
    fun provideGetShopOrdersUseCase(orderRepository: OrderRepository): GetShopOrdersUseCase {
        return GetShopOrdersUseCase(orderRepository)
    }

    @Provides
    @Singleton
    fun provideMarkOrderAsDeliveredUseCase(orderRepository: OrderRepository): MarkOrderAsDeliveredUseCase {
        return MarkOrderAsDeliveredUseCase(orderRepository)
    }

    // Inventory Use Cases
    @Provides
    @Singleton
    fun provideUpdateStockUseCase(inventoryRepository: InventoryRepository): UpdateStockUseCase {
        return UpdateStockUseCase(inventoryRepository)
    }

    @Provides
    @Singleton
    fun provideCheckLowStockUseCase(inventoryRepository: InventoryRepository): CheckLowStockUseCase {
        return CheckLowStockUseCase(inventoryRepository)
    }

    @Provides
    @Singleton
    fun provideSyncInventoryUseCase(inventoryRepository: InventoryRepository): SyncInventoryUseCase {
        return SyncInventoryUseCase(inventoryRepository)
    }

    // Chat Use Cases
    @Provides
    @Singleton
    fun provideSendMessageUseCase(chatRepository: ChatRepository): SendMessageUseCase {
        return SendMessageUseCase(chatRepository)
    }

    @Provides
    @Singleton
    fun provideGetChatMessagesUseCase(chatRepository: ChatRepository): GetChatMessagesUseCase {
        return GetChatMessagesUseCase(chatRepository)
    }

    @Provides
    @Singleton
    fun provideMarkAsReadUseCase(chatRepository: ChatRepository): MarkAsReadUseCase {
        return MarkAsReadUseCase(chatRepository)
    }

    // Analytics Use Cases
    @Provides
    @Singleton
    fun provideGetShopAnalyticsUseCase(analyticsRepository: AnalyticsRepository): GetShopAnalyticsUseCase {
        return GetShopAnalyticsUseCase(analyticsRepository)
    }

    @Provides
    @Singleton
    fun provideGetTopProductsUseCase(analyticsRepository: AnalyticsRepository): GetTopProductsUseCase {
        return GetTopProductsUseCase(analyticsRepository)
    }

    @Provides
    @Singleton
    fun provideGenerateReportUseCase(analyticsRepository: AnalyticsRepository): GenerateReportUseCase {
        return GenerateReportUseCase(analyticsRepository)
    }
}

