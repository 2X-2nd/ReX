# Example M5: Testing and Code Review

## 1. Change History

| **Change Date**   | **Modified Sections** | **Rationale** |
| ----------------- | --------------------- | ------------- |
| _Nothing to show_ |

---

## 2. Back-end Test Specification: APIs

### 2.1. Locations of Back-end Tests and Instructions to Run Them

#### 2.1.1. Tests

| **Interface**                         | **Describe Group Location, No Mocks**                  | **Describe Group Location, With Mocks**              | **Mocked Components**              |
| ------------------------------------- | ---------------------------------------------------- | -------------------------------------------------- | ---------------------------------- |
| **POST /users/register**             | [`backend/tests/userNM.test.js#L35`](#)    | [`backend/tests/mocked/userM.test.js#L65`](#)    | Database (MySQL)                  |
| **GET /users/:id**                   | [`backend/tests/userNM.test.js#L80`](#)    | [`backend/tests/mocked/userM.test.js#L100`](#)   | Database (MySQL)                  |
| **PUT /users/:id**                   | [`backend/tests/userNM.test.js#L120`](#)   | [`backend/tests/mocked/userM.test.js#L150`](#)   | Database (MySQL)                  |
| **DELETE /users/:id**                | [`backend/tests/userNM.test.js#L160`](#)   | [`backend/tests/mocked/userM.test.js#L200`](#)   | Database (MySQL)                  |
| **GET /recommendations/:userId**     | [`backend/tests/recommendationNM.test.js#L35`](#)  | [`backend/tests/recommendationM.test.js#L65`](#)  | User Service (API), Listings Service (API) |
| **POST /price-suggestions**          | [`backend/tests/recommendationNM.test.js#L90`](#)  | [`backend/tests/recommendationM.test.js#L130`](#) | eBay API (SerpAPI)                 |
| **GET /price-comparison/:itemId**    | [`backend/tests/recommendationNM.test.js#L150`](#) | [`backend/tests/recommendationM.test.js#L200`](#) | Listings Service (API), eBay API (SerpAPI) |

