# M3 - Requirements and Design

## 1. Change History
<!-- Leave blank for M3 -->

## 2. Project Description
We intend to design a second-hand marketplace app for people looking to buy or sell unwanted items locally. When you need to make space, whether because you are looking to relocate or declutter, our storing service will take your items off your hands immediately. Sellers can deposit their items in nearby partner warehouses, where they are securely stored until sold. This ensures valuable items are not wasted and allows users to focus on their next steps without stress.  

The app integrates Google Maps API to locate storage facilities near the seller's current position, making it easy to choose the most convenient option. Once stored, items are listed on our platform with detailed descriptions and photos. Additionally, our app offers a price recommendation and comparison feature. It analyzes our database of similar second-hand items and their past transaction prices to suggest a fair and competitive price for sellers. Furthermore, the app compares the recommended price with the market price of similar new items from other platforms, highlighting how much buyers can save by purchasing through our platform. This innovative approach not only ensures competitive pricing for sellers but also maximizes buyer satisfaction by demonstrating the cost-effectiveness of purchasing second-hand items through our app.  

Buyers benefit from a personalized recommendation system and real-time chat functionality, ensuring smooth communication and a tailored shopping experience. Whether buyers prefer self-pickup from the warehouse or delivery, the app simplifies the entire process, making second-hand trading faster, more efficient, and ultimately more rewarding for all users.  
## 3. Requirements Specification
### **3.1. Use-Case Diagram**
![Use Case Diagram](images/Use_Case_Diagram.jpg)

### **3.2. Actors Description**
1. **[BUYERS]**: Buyers are users looking to purchase second-hand items. They can browse listings, communicate with sellers, and choose pickup or delivery options.
2. **[SELLERS]**: Sellers are users who wish to list unwanted items for sale. They can deposit their items in partner warehouses, post product details, and manage their listings.
3. **[Administrator]**：The administrator ensures the smooth operation of the platform, including monitoring listings, managing warehouses, and handling user disputes.



### **3.3. Functional Requirements**
<a name="fr1"></a>

1. **[Register and Login]** 
    - **Overview**:
        1. User registration
        2. User login

    - **Detailed Flow for Each Independent Scenario**: 
        1. **[User Registration]**:
            - **Description**: Users create an account by providing their email, password, and other required details.
            - **Primary actor(s)**: Sellers, Buyers
            - **Main success scenario**:
                1. User opens the app and selects the "Register" option.
                2. User enters their details (e.g., email, password) and submits.
                3. The system verifies the details and creates the account successfully.
                4. The system verifies the details and creates the account successfully.

            - **Failure scenario(s)**:
                - 1a. User inputs invalid data (e.g., weak password, already registered email).
                    - 1a1. System displays an error message indicating the issue.
                    - 1a2. User is prompted to correct the error and try again.

                - 1b. System is down during registration.
                    - 1b1. User receives a message indicating temporary unavailability.
                    - 1b2. User is asked to try again later.
                
        2. **[User Login]**:
            - **Description**: Returning users log in to access their accounts.
            - **Primary actor(s)**: Sellers, Buyers
            - **Main success scenario**:
                1. User selects the "Login" option.
                2. User inputs their email and password.
                3. The system verifies credentials and grants access to the account.

            - **Failure scenario(s)**:
                - 2a. Incorrect email or password entered
                    - 2a1. System displays an error message indicating invalid credentials.
                    - 2a2. User retries with the correct credentials.

                - 2b. System is unavailable.
                    - 2b1. User receives a message indicating temporary downtime.
                    - 2b2. User is advised to try again later.

2. **[List an Item for Sale]** 
    - **Overview**:
        1. Upload item details
        2. Price recommendation

    - **Detailed Flow for Each Independent Scenario**: 
        1. **[User Registration]**:
            - **Description**: Sellers can upload item photos, descriptions, and set prices.
            - **Primary actor(s)**: Sellers
            - **Main success scenario**:
                1. Seller selects the "List Item" option.
                2. Seller uploads photos and fills in item details (e.g., description).
                3. The system verifies the details and accepts the listing.

            - **Failure scenario(s)**:
                - 1a. Seller uploads incomplete or invalid details.
                    - 1a1. System displays an error message highlighting missing/invalid fields.
                    - 1a2. Seller corrects the errors and resubmits the details.
                - 1b. System is down during registration.
                    - 1b1. User receives a message indicating temporary unavailability.
                    - 1b2. User is asked to try again later.
                
        2. **[Price Recommendation]**:
            - **Description**: The system suggests a competitive price based on database analysis and comparison with new item prices on other platforms.
            - **Primary actor(s)**: Sellers
            - **Main success scenario**:
                1. Seller enters item details and clicks "Get Price Recommendation".
                2. System analyzes the database and provides a price suggestion.
                3. System also compares the price to new item prices and displays the difference.
                4. Seller accepts the recommendation or sets a custom price.

            - **Failure scenario(s)**:
                - 2a. Price recommendation fails due to missing database data.
                    - 2a1. System notifies the seller about the issue.
                    - 2a2. Seller can proceed without the recommendation.

                - 2b. System cannot fetch competitor prices.
                    - 2b1. System displays a message about unavailability of competitor price data.
                    - 2b2. Seller proceeds with the recommended price based on internal data.
3. **[Locate Storage Facilities]**

- **Overview**:
    1. Find nearby warehouses
    2. Schedule item drop-off

- **Detailed Flow for Each Independent Scenario**:

    1. **[Find Nearby Warehouses]**:
        - **Description**: Sellers locate storage facilities near their current location using the Google Maps API.
        - **Primary actor(s)**: Sellers
        - **Main success scenario**:
            1. Seller selects the "Find Storage" option.
            2. System uses the seller's location to list nearby warehouses.
            3. Seller selects a preferred facility.
        - **Failure scenario(s)**:
            - **1a.** Location permissions are denied.
                - **1a1.** System prompts the user to enable location services.
                - **1a2.** Seller enables location services and retries.
            - **1b.** No warehouses available nearby.
                - **1b1.** System displays a message stating unavailability of storage options.
                - **1b2.** Seller can retry later or contact support.

    2. **[Schedule Item Drop-Off]**:
        - **Description**: Sellers schedule a time to deposit items at a selected warehouse.
        - **Primary actor(s)**: Sellers
        - **Main success scenario**:
            1. Seller selects a warehouse and chooses a convenient drop-off time.
            2. System confirms the booking and sends a confirmation message.
        - **Failure scenario(s)**:
            - **2a.** Selected time slot is unavailable.
                - **2a1.** System suggests alternative time slots.
                - **2a2.** Seller selects a new time and proceeds.
            - **2b.** System fails to confirm the booking due to server issues.
                - **2b1.** Seller receives a notification about the issue.
                - **2b2.** Seller retries later or contacts support.



### **3.4. Screen Mockups**



### **3.5. Non-Functional Requirements**
<a name="nfr1"></a>

1. **[WRITE_NAME_HERE]**
    - **Description**: ...
    - **Justification**: ...
2. ...


## 4. Design Specification
### **4.1. Main Components**
1. **Marketplace Service**
    - **Purpose**: Handles product listings, pricing, and search functionality.
    - **Interfaces**:
        - `POST /listings` - Creates a new product listing.
        - `GET /listings/{id}` - Retrieves details of a specific listing.
        - `PUT /listings/{id}` - Updates an existing listing.
        - `DELETE /listings/{id}` - Removes a listing from the marketplace.
        - `GET /listings/search?query={query}` - Searches for listings using keywords.

2. **Storage Management Service**
    - **Purpose**: Manages warehouse storage and assigns storage locations for stored items.
    - **Interfaces**:
        - `POST /storage/request` - Requests storage space for an item.
        - `GET /storage/status/{itemId}` - Retrieves storage status of an item.
        - `DELETE /storage/{itemId}` - Removes an item from storage after sale or withdrawal.

3. **User Service**
    - **Purpose**: Manages authentication, profiles, and permissions.
    - **Interfaces**:
        - `POST /users/register` - Creates a new user account.
        - `POST /users/login` - Authenticates a user and returns a token.
        - `GET /users/{id}` - Retrieves user profile information.
        - `PUT /users/{id}` - Updates user profile details.
        - `DELETE /users/{id}` - Deletes a user account.

4. **Recommendation Engine**
    - **Purpose**: Provides price suggestions and personalized recommendations.
    - **Interfaces**:
        - `GET /recommendations/{userId}` - Fetches personalized item recommendations.
        - `POST /price-suggestions` - Suggests a price for a new listing.
        - `GET /price-comparison/{itemId}` - Retrieves price comparisons from external sources.

5. **Chat Service**
    - **Purpose**: Manages buyer-seller chat functionality.
    - **Interfaces**:
        - `POST /chat/start` - Initiates a chat between a buyer and seller.
        - `GET /chat/{chatId}` - Retrieves chat history.
        - `POST /chat/{chatId}/message` - Sends a new message.

### **4.4. Frameworks**
- **Cloud Provider**: AWS
- **Backend Framework**: Node.js with Express.js
- **Frontend Framework**: Native Kotlin for Android
- **Databases**: MongoDB (Atlas) for chat, MySQL (AWS RDS) for listings & user data
- **Other Tools**: Docker, Kubernetes, Redis, Firebase Auth, AWS API Gateway

### **4.5. Dependencies Diagram**


### **4.6. Functional Requirements Sequence Diagram**
1. [**[WRITE_NAME_HERE]**](#fr1)\
[SEQUENCE_DIAGRAM_HERE]
2. ...


### **4.7. Non-Functional Requirements Design**
1. [**[WRITE_NAME_HERE]**](#nfr1)
    - **Validation**: ...
2. ...


### **4.8. Main Project Complexity Design**
**[WRITE_NAME_HERE]**
- **Description**: ...
- **Why complex?**: ...
- **Design**:
    - **Input**: ...
    - **Output**: ...
    - **Main computational logic**: ...
    - **Pseudo-code**: ...
        ```

        ```


## 5. Contributions
- ...
- ...
- ...
- ...

