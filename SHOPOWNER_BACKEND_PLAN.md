# Shop Owner Backend Implementation Plan

## System Overview

The OneCornerSystem is a two-sided marketplace with the following core modules:

```
┌─────────────────────────────────────────────────────────────┐
│           OneCornerSystem - Two-Sided Marketplace           │
├─────────────────────┬───────────────────────────────────────┤
│   CUSTOMER MODULE   │      SHOP OWNER MODULE (Focus)        │
├─────────────────────┼───────────────────────────────────────┤
│ • Browse Shops      │ • Shop Management                     │
│ • Browse Products   │ • Product Management (CRUD)           │
│ • Add to Cart       │ • Inventory Management                │
│ • Place Orders      │ • Order Management & Tracking         │
│ • Track Orders      │ • Customer Support (Chat)             │
│ • Chat Support      │ • Shop Analytics & Insights           │
│                     │ • Real-Time Status Updates            │
└─────────────────────┴───────────────────────────────────────┘
```

## Scope: Shop Owner Backend Operations

### ✅ In Scope (Backend Only)
- Shop data models and Firestore collections
- Product CRUD operations with variants
- Order receiving and status management
- Inventory tracking and sync
- Real-time messaging (shop ↔ customer)
- Shop analytics and insights
- Repository patterns and business logic
- Firestore security rules
- Real-time listeners and state management

### ❌ Out of Scope (For Later)
- Frontend UI/Compose screens (Shop management interface)
- Customer module backend
- Payment processing integration
- Email/SMS notifications
- Firebase Cloud Functions (defer to Phase 2)
- Third-party integrations

---

## Phase 1: Core Data Models

### Required Firestore Collections

```
/shops/{shopId}
├── shopId: String
├── ownerId: String (ref to User.userId)
├── shopName: String
├── category: String
├── location: GeoPoint {latitude, longitude}
├── address: String
├── contactNumber: String
├── profileImage: String
├── rating: Double
├── totalReviews: Int
├── isActive: Boolean
├── description: String
├── operatingHours: Map {day -> {open, close}}
├── createdAt: Timestamp
├── updatedAt: Timestamp
└── /products/{productId}  [Subcollection]
    ├── /variants/{variantId}  [Sub-subcollection]
    └── /reviews/{reviewId}  [Sub-subcollection]

/orders/{orderId}
├── orderId: String
├── userId: String (ref to Customer)
├── shopId: String (ref to Shop)
├── customerName: String (snapshot at order time)
├── customerPhone: String
├── totalAmount: Double
├── subtotal: Double
├── deliveryFee: Double
├── taxAmount: Double
├── discount: Double
├── status: String [pending, accepted, preparing, ready, out_for_delivery, delivered, cancelled]
├── paymentStatus: String [pending, completed, failed]
├── deliveryAddress: String
├── estimatedDelivery: Timestamp
├── createdAt: Timestamp
├── updatedAt: Timestamp
└── /items/{itemId}  [Subcollection]
    ├── productId: String
    ├── variantId: String (if applicable)
    ├── productName: String (snapshot)
    ├── quantity: Int
    ├── price: Double (snapshot at order time)
    └── totalPrice: Double

/chats/{chatId}
├── chatId: String
├── userId: String (customer)
├── shopId: String (shop)
├── lastMessage: String
├── lastMessageTime: Timestamp
├── isActive: Boolean
├── createdAt: Timestamp
├── updatedAt: Timestamp
└── /messages/{messageId}  [Subcollection]
    ├── messageId: String
    ├── senderId: String (userId or shopId as reference)
    ├── senderType: String [customer, shop_owner]
    ├── text: String
    ├── attachmentUrl: String (optional)
    ├── isRead: Boolean
    ├── timeSent: Timestamp
    └── updatedAt: Timestamp

/inventory/{shopId}
├── shopId: String
├── totalProducts: Int
├── lowStockAlert: Int (threshold)
├── lastSyncTime: Timestamp
└── (Note: Individual product stock stored in Product collection)

/shopAnalytics/{shopId}
├── shopId: String
├── totalOrders: Int
├── totalRevenue: Double
├── averageOrderValue: Double
├── totalCustomers: Int
├── repeatCustomerRate: Double
├── averageRating: Double
├── peakHours: Map {hour -> count}
├── topProducts: List<{productId, count}>
├── weeklyStats: List {date, orders, revenue}
├── monthlyStats: List {month, orders, revenue}
├── updatedAt: Timestamp
```

---

## Phase 2: Data Model Classes (Kotlin)

### Models to Create

1. **Shop.kt** - Shop entity
2. **Product.kt** - Product entity with variants
3. **ProductVariant.kt** - Product variant details
4. **Order.kt** - Order entity
5. **OrderItem.kt** - Individual order items
6. **Chat.kt** - Chat session
7. **Message.kt** - Chat messages
8. **Inventory.kt** - Inventory tracking
9. **ShopAnalytics.kt** - Analytics data
10. **ShopOwnerDashboard.kt** - Dashboard summary

### Key Features of Models
- All models use Firestore-compatible data types
- Timestamp for all date fields (Firestore native)
- Immutable data classes with copy() support
- Default values for optional fields
- Serialization support for Firestore

---

## Phase 3: Repository Layer

### Repositories to Create

#### 1. ShopRepository
**Responsibilities:**
- Create shop
- Get shop details
- Update shop profile
- Get all products for shop
- Deactivate shop

**Methods:**
```kotlin
fun createShop(shop: Shop): Flow<Resource<Shop>>
fun getShopDetails(shopId: String): Flow<Resource<Shop>>
fun updateShopProfile(shopId: String, updates: Map<String, Any>): Flow<Resource<Boolean>>
fun getShopRating(shopId: String): Flow<Resource<Double>>
fun updateShopIsActive(shopId: String, isActive: Boolean): Flow<Resource<Boolean>>
fun listenToShopDetails(shopId: String): Flow<Shop?>
```

#### 2. ProductRepository
**Responsibilities:**
- Create product
- Update product
- Delete product
- Get product list
- Get product details
- Manage variants
- Update pricing

**Methods:**
```kotlin
fun createProduct(product: Product): Flow<Resource<Product>>
fun getProduct(productId: String, shopId: String): Flow<Resource<Product>>
fun updateProduct(productId: String, shopId: String, updates: Map<String, Any>): Flow<Resource<Boolean>>
fun deleteProduct(productId: String, shopId: String): Flow<Resource<Boolean>>
fun getShopProducts(shopId: String): Flow<Resource<List<Product>>>
fun searchProducts(shopId: String, query: String): Flow<Resource<List<Product>>>
fun addVariant(productId: String, shopId: String, variant: ProductVariant): Flow<Resource<ProductVariant>>
fun updateVariant(productId: String, variantId: String, shopId: String, updates: Map<String, Any>): Flow<Resource<Boolean>>
fun deleteVariant(productId: String, variantId: String, shopId: String): Flow<Resource<Boolean>>
fun listenToShopProducts(shopId: String): Flow<List<Product>>
```

#### 3. OrderRepository
**Responsibilities:**
- Get shop orders
- Get order details
- Update order status
- Accept/reject orders
- Track order timeline
- Listen to real-time orders

**Methods:**
```kotlin
fun getShopOrders(shopId: String, status: String? = null): Flow<Resource<List<Order>>>
fun getOrderDetails(orderId: String, shopId: String): Flow<Resource<Order>>
fun updateOrderStatus(orderId: String, shopId: String, newStatus: String): Flow<Resource<Boolean>>
fun acceptOrder(orderId: String, shopId: String): Flow<Resource<Boolean>>
fun rejectOrder(orderId: String, shopId: String, reason: String): Flow<Resource<Boolean>>
fun getOrderItems(orderId: String): Flow<Resource<List<OrderItem>>>
fun listenToShopOrders(shopId: String): Flow<List<Order>>
fun listenToOrderDetails(orderId: String): Flow<Order?>
```

#### 4. InventoryRepository
**Responsibilities:**
- Update stock quantity
- Check stock availability
- Get low stock alerts
- Sync inventory

**Methods:**
```kotlin
fun updateStockQuantity(productId: String, shopId: String, quantity: Int): Flow<Resource<Boolean>>
fun updateVariantStock(productId: String, variantId: String, shopId: String, quantity: Int): Flow<Resource<Boolean>>
fun getStockStatus(productId: String, shopId: String): Flow<Resource<Int>>
fun getLowStockProducts(shopId: String): Flow<Resource<List<Product>>>
fun syncInventory(shopId: String): Flow<Resource<Boolean>>
```

#### 5. ChatRepository
**Responsibilities:**
- Get chat sessions
- Get messages from chat
- Send message
- Mark message as read
- Listen to real-time messages

**Methods:**
```kotlin
fun getShopChats(shopId: String): Flow<Resource<List<Chat>>>
fun getChatMessages(chatId: String): Flow<Resource<List<Message>>>
fun sendMessage(chatId: String, message: Message): Flow<Resource<Message>>
fun markMessageAsRead(chatId: String, messageId: String): Flow<Resource<Boolean>>
fun listenToChatMessages(chatId: String): Flow<List<Message>>
fun listenToShopChats(shopId: String): Flow<List<Chat>>
fun deleteChat(chatId: String): Flow<Resource<Boolean>>
```

#### 6. AnalyticsRepository
**Responsibilities:**
- Get shop analytics
- Calculate metrics
- Track events
- Generate reports

**Methods:**
```kotlin
fun getShopAnalytics(shopId: String): Flow<Resource<ShopAnalytics>>
fun getTodayStats(shopId: String): Flow<Resource<DailyStat>>
fun getWeeklyStats(shopId: String): Flow<Resource<List<DailyStat>>>
fun getMonthlyStats(shopId: String): Flow<Resource<List<MonthlyStat>>>
fun getTopProducts(shopId: String, limit: Int = 10): Flow<Resource<List<ProductStat>>>
fun getCustomerMetrics(shopId: String): Flow<Resource<CustomerMetrics>>
fun updateAnalytics(shopId: String, order: Order): Flow<Resource<Boolean>>
```

---

## Phase 4: Business Logic & Use Cases

### Use Case Layer (Domain)

Create use case classes for each major operation:

```
/domain/usecase/
├── shop/
│   ├── CreateShopUseCase.kt
│   ├── UpdateShopProfileUseCase.kt
│   ├── GetShopDetailsUseCase.kt
│   └── DeactivateShopUseCase.kt
├── product/
│   ├── CreateProductUseCase.kt
│   ├── UpdateProductUseCase.kt
│   ├── DeleteProductUseCase.kt
│   ├── AddProductVariantUseCase.kt
│   ├── UpdatePricingUseCase.kt
│   └── ListProductsUseCase.kt
├── order/
│   ├── AcceptOrderUseCase.kt
│   ├── RejectOrderUseCase.kt
│   ├── UpdateOrderStatusUseCase.kt
│   ├── GetShopOrdersUseCase.kt
│   └── MarkOrderAsDeliveredUseCase.kt
├── inventory/
│   ├── UpdateStockUseCase.kt
│   ├── CheckLowStockUseCase.kt
│   └── SyncInventoryUseCase.kt
├── chat/
│   ├── SendMessageUseCase.kt
│   ├── GetChatMessagesUseCase.kt
│   └── MarkAsReadUseCase.kt
└── analytics/
    ├── GetShopAnalyticsUseCase.kt
    ├── GetTopProductsUseCase.kt
    └── GenerateReportUseCase.kt
```

### Validation Rules

**Shop Creation:**
- Shop name required and min 3 characters
- Category must be valid (predefined list)
- Location (GeoPoint) required
- Contact number valid format
- Owner must exist in users collection

**Product Creation:**
- Product name required, min 3 characters
- Price > 0
- SKU must be unique within shop
- Category required
- At least one image required
- Stock quantity >= 0

**Order Status Updates:**
- Only shop owner can accept/reject own orders
- Status transitions follow defined workflow:
  - pending → accepted OR rejected
  - accepted → preparing OR cancelled
  - preparing → ready OR cancelled
  - ready → out_for_delivery OR cancelled
  - out_for_delivery → delivered OR cancelled

**Inventory Operations:**
- Stock quantity cannot go negative
- Alert when stock below threshold
- Prevent overselling

**Chat Operations:**
- Only shop owner and customer in chat can send messages
- Cannot modify sent messages (immutable)
- Cannot delete messages (only archive)

---

## Phase 5: Real-Time Sync & State Management

### Create ViewModels for Shop Owner

```
/presentation/shopowner/viewmodel/
├── ShopViewModel.kt (manage shop data)
├── ProductViewModel.kt (manage products)
├── OrderViewModel.kt (manage orders)
├── ChatViewModel.kt (manage messages)
├── InventoryViewModel.kt (manage stock)
└── AnalyticsViewModel.kt (manage dashboard)
```

### State Management Pattern

Each ViewModel follows this pattern:

```kotlin
@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository
) : ViewModel() {
    
    private val _shopState = MutableStateFlow<Resource<Shop>>(Resource.Idle)
    val shopState: StateFlow<Resource<Shop>> = _shopState
    
    fun initializeShop(shopId: String) {
        viewModelScope.launch {
            shopRepository.listenToShopDetails(shopId)
                .collect { shop ->
                    _shopState.value = Resource.Success(shop ?: error)
                }
        }
    }
    
    fun updateShop(updates: Map<String, Any>) {
        viewModelScope.launch {
            shopRepository.updateShopProfile(_shopId, updates)
                .collect { result ->
                    // Handle result
                }
        }
    }
}
```

### Real-Time Listeners

Key areas needing real-time listeners:
1. **Orders** - New orders, status changes
2. **Messages** - Incoming customer messages
3. **Inventory Alerts** - Low stock warnings
4. **Shop Details** - Profile updates from other devices
5. **Product Stock** - Real-time stock changes

---

## Phase 6: Firestore Security Rules

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // ==================== SHOPS ====================
    match /shops/{shopId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && 
                       request.resource.data.ownerId == request.auth.uid;
      allow update: if request.auth != null && 
                       request.resource.data.ownerId == request.auth.uid &&
                       resource.data.ownerId == request.auth.uid;
      allow delete: if request.auth != null && 
                       resource.data.ownerId == request.auth.uid;
      
      // =============== PRODUCTS ===============
      match /products/{productId} {
        allow read: if request.auth != null;
        allow create, update, delete: if request.auth != null && 
                                         get(/databases/$(database)/documents/shops/$(shopId)).data.ownerId == request.auth.uid;
        
        // ============= VARIANTS =============
        match /variants/{variantId} {
          allow read: if request.auth != null;
          allow create, update, delete: if request.auth != null && 
                                           get(/databases/$(database)/documents/shops/$(shopId)).data.ownerId == request.auth.uid;
        }
        
        // ============= REVIEWS =============
        match /reviews/{reviewId} {
          allow read: if request.auth != null;
          allow create: if request.auth != null && 
                           request.resource.data.userId == request.auth.uid;
          allow update, delete: if request.auth != null && 
                                   resource.data.userId == request.auth.uid;
        }
      }
    }
    
    // ==================== ORDERS ====================
    match /orders/{orderId} {
      allow read: if request.auth != null && 
                     (resource.data.userId == request.auth.uid || 
                      get(/databases/$(database)/documents/shops/$(resource.data.shopId)).data.ownerId == request.auth.uid);
      allow create: if request.auth != null && 
                       request.resource.data.userId == request.auth.uid;
      allow update: if request.auth != null && 
                       get(/databases/$(database)/documents/shops/$(resource.data.shopId)).data.ownerId == request.auth.uid;
      allow delete: if false; // Orders cannot be deleted
      
      // =============== ORDER ITEMS ===============
      match /items/{itemId} {
        allow read: if request.auth != null && 
                       (getAfter(/databases/$(database)/documents/orders/$(orderId)).data.userId == request.auth.uid || 
                        get(/databases/$(database)/documents/shops/$(getAfter(/databases/$(database)/documents/orders/$(orderId)).data.shopId)).data.ownerId == request.auth.uid);
      }
    }
    
    // ==================== CHATS ====================
    match /chats/{chatId} {
      allow read: if request.auth != null && 
                     (resource.data.userId == request.auth.uid);
      allow create: if request.auth != null && 
                       request.resource.data.userId == request.auth.uid;
      
      // =============== MESSAGES ===============
      match /messages/{messageId} {
        allow read: if request.auth != null && 
                       (get(/databases/$(database)/documents/chats/$(chatId)).data.userId == request.auth.uid ||
                        get(/databases/$(database)/documents/chats/$(chatId)).data.shopId == request.auth.uid);
        allow create: if request.auth != null && 
                        (get(/databases/$(database)/documents/chats/$(chatId)).data.userId == request.auth.uid ||
                         get(/databases/$(database)/documents/chats/$(chatId)).data.shopId == request.auth.uid);
      }
    }
    
    // ==================== INVENTORY ====================
    match /inventory/{shopId} {
      allow read: if request.auth != null && 
                     get(/databases/$(database)/documents/shops/$(shopId)).data.ownerId == request.auth.uid;
      allow update: if request.auth != null && 
                       get(/databases/$(database)/documents/shops/$(shopId)).data.ownerId == request.auth.uid;
    }
    
    // ==================== ANALYTICS ====================
    match /shopAnalytics/{shopId} {
      allow read: if request.auth != null && 
                     get(/databases/$(database)/documents/shops/$(shopId)).data.ownerId == request.auth.uid;
      allow update: if request.auth != null && 
                       get(/databases/$(database)/documents/shops/$(shopId)).data.ownerId == request.auth.uid;
    }
  }
}
```

---

## Phase 7: Dependency Injection Setup

Update the Hilt module to include all repositories:

```kotlin
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
    fun provideInventoryRepository(firestore: FirebaseFirestore): InventoryRepository {
        return InventoryRepositoryImpl(firestore)
    }
    
    @Provides
    @Singleton
    fun provideChatRepository(firestore: FirebaseFirestore): ChatRepository {
        return ChatRepositoryImpl(firestore)
    }
    
    @Provides
    @Singleton
    fun provideAnalyticsRepository(firestore: FirebaseFirestore): AnalyticsRepository {
        return AnalyticsRepositoryImpl(firestore)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    @Provides
    fun provideCreateProductUseCase(productRepository: ProductRepository): CreateProductUseCase {
        return CreateProductUseCase(productRepository)
    }
    
    // ... other use cases
}
```

---

## Implementation Sequence (Priority)

### Priority 1: Foundation (Week 1)
1. ✅ Create Shop.kt data model
2. ✅ Create Product.kt and ProductVariant.kt models
3. ✅ Create Order.kt and OrderItem.kt models
4. ✅ Create Chat.kt and Message.kt models
5. ✅ Setup Firestore collections structure

### Priority 2: Core Repositories (Week 2)
1. ✅ ShopRepository - create, read, update, listen
2. ✅ ProductRepository - CRUD operations
3. ✅ OrderRepository - get, status updates, listen
4. ✅ ChatRepository - messages, listen

### Priority 3: Inventory & Analytics (Week 3)
1. ✅ InventoryRepository - stock management
2. ✅ AnalyticsRepository - metrics and reports
3. ✅ Real-time sync for all repositories

### Priority 4: Security & Validation (Week 4)
1. ✅ Firestore security rules
2. ✅ Input validation in repositories
3. ✅ Error handling and logging

### Priority 5: Use Cases & ViewModels (Week 5)
1. ✅ Domain use cases layer
2. ✅ ViewModels for each module
3. ✅ State management integration

---

## Testing Strategy

### Unit Tests
- Validate data models serialization
- Test business logic validation rules
- Test use case orchestration

### Integration Tests
- Test Firestore operations
- Test repository methods
- Test real-time listeners

### Security Tests
- Validate Firestore rules
- Test unauthorized access prevention
- Test data isolation

---

## Deliverables

```
backend/
├── models/
│   ├── Shop.kt ✓
│   ├── Product.kt ✓
│   ├── ProductVariant.kt ✓
│   ├── Order.kt ✓
│   ├── OrderItem.kt ✓
│   ├── Chat.kt ✓
│   ├── Message.kt ✓
│   ├── Inventory.kt ✓
│   ├── ShopAnalytics.kt ✓
│   └── Dashboard.kt
├── repository/
│   ├── ShopRepository.kt + ShopRepositoryImpl.kt
│   ├── ProductRepository.kt + ProductRepositoryImpl.kt
│   ├── OrderRepository.kt + OrderRepositoryImpl.kt
│   ├── ChatRepository.kt + ChatRepositoryImpl.kt
│   ├── InventoryRepository.kt + InventoryRepositoryImpl.kt
│   └── AnalyticsRepository.kt + AnalyticsRepositoryImpl.kt
├── domain/
│   ├── usecase/ (12+ use cases)
│   └── validators/ (business rules)
├── di/
│   ├── RepositoryModule.kt
│   └── UseCaseModule.kt
├── viewmodel/
│   ├── ShopViewModel.kt
│   ├── ProductViewModel.kt
│   ├── OrderViewModel.kt
│   ├── ChatViewModel.kt
│   ├── InventoryViewModel.kt
│   └── AnalyticsViewModel.kt
├── firestore/
│   └── FirestoreSecurityRules.txt
└── tests/
    ├── unit/
    ├── integration/
    └── security/
```

---

## Success Criteria

✅ All Firestore collections properly structured
✅ All repositories implement Flow-based async operations
✅ Real-time listeners working for critical data
✅ Firestore security rules enforced
✅ All operations validated and error-handled
✅ ViewModels integrated with repositories
✅ Analytics calculations correct
✅ Order status workflow validated
✅ Chat operations working bi-directionally
✅ Inventory sync functional

