# Testing Patterns

**Analysis Date:** 2026-03-15

## Test Framework

**Unit Test Runner:**
- JUnit 4 (`junit:junit:4.13.2`)
- Robolectric (`robolectric:4.14.1`) for Android-aware unit tests
- Config: `build.gradle.kts` with `testOptions.unitTests.isIncludeAndroidResources = true`

**UI Test Runner:**
- AndroidJUnit4 for Compose UI tests
- Compose testing: `androidx.compose.ui:ui-test-junit4`

**Assertion Library:**
- JUnit assertions: `org.junit.Assert.*`
- Direct equality checks

**Run Commands:**
```powershell
.\gradlew.bat testDebugUnitTest                                    # Run all unit tests
.\gradlew.bat testDebugUnitTest --tests "*.DomainLogicTest"       # Run specific test class
.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.peter.overtimecalculator.MainSmokeTest
```

## Test File Organization

**Location:**
- Unit tests: `app/src/test/java/com/peter/overtimecalculator/`
- Android (UI) tests: `app/src/androidTest/java/com/peter/overtimecalculator/`

**Naming:**
- Source file: `OvertimeViewModel.kt`
- Test file: `OvertimeViewModelTest.kt` (or similar pattern)
- Suffix: `Test` for test classes

**Structure:**
```
app/src/test/java/com/peter/overtimecalculator/
├── DomainLogicTest.kt
├── WriteUseCaseTest.kt
├── CenteredDurationSliderTest.kt
├── HolidayRulesRepositoryTest.kt
└── ...
```

## Test Structure

**Suite Organization:**
```kotlin
class DomainLogicTest {
    private val holidayCalendar = HolidayCalendar { ... }

    @Test
    fun payFormula_usesConfiguredMultiplier() {
        // Arrange
        val config = testConfig(hourlyRate = decimal("50.00"))
        
        // Act & Assert
        assertDecimalEquals("150.00", PayFormula.calculatePay(...))
    }

    private fun testConfig(...): MonthlyConfig { ... }
    private fun assertDecimalEquals(expected: String, actual: BigDecimal) { ... }
}
```

**Patterns:**
- Private helper functions for test data creation
- Private assertion helpers for domain-specific checks
- One test class per domain concept or feature

## Mocking

**Framework:** Manual fake implementations (no mocking library)

**Patterns:**
```kotlin
// Fake implementation injected into use cases
private class FakeOvertimeWriteGateway(
    private val resolvedDayType: DayType = DayType.WORKDAY,
) : OvertimeWriteGateway {
    var saveOvertimeCalled = false
    
    override suspend fun saveOvertime(...) {
        saveOvertimeCalled = true
    }
    
    override suspend fun updateManualHourlyRate(...) {
        updateManualHourlyRateCalled = true
    }
    
    override fun resolveDayType(...): DayType = resolvedDayType
}
```

**What to Mock:**
- `OvertimeWriteGateway` - Repository write operations
- `HolidayRemoteClient` - Network client

**What NOT to Mock:**
- Domain logic (calculators, validators) - test directly
- Data classes and value objects

## Fixtures and Factories

**Test Data:**
```kotlin
private fun testConfig(
    yearMonth: YearMonth = YearMonth.of(2026, 10),
    hourlyRate: BigDecimal = decimal("60.00"),
    lockedByUser: Boolean = false,
): MonthlyConfig {
    return MonthlyConfig(
        yearMonth = yearMonth,
        hourlyRate = hourlyRate,
        rateSource = HourlyRateSource.MANUAL,
        weekdayRate = decimal("1.50"),
        restDayRate = decimal("2.00"),
        holidayRate = decimal("3.00"),
        lockedByUser = lockedByUser,
    )
}
```

**Location:**
- Private helper functions within test classes
- No shared fixtures across test classes

**Decimal Helper:**
```kotlin
fun decimal(value: String): BigDecimal = BigDecimal(value)
```

## Coverage

**Requirements:** None enforced

**View Coverage:**
```powershell
.\gradlew.bat testDebugUnitTest --coverage
```

Note: Coverage reports require additional configuration.

## Test Types

**Unit Tests:**
- Domain logic: calculators, validators, use cases
- Pure functions and data transformations
- Located in `src/test/`

**Integration Tests:**
- Repository with in-memory or temporary storage
- Uses `TemporaryFolder` rule for isolated file system
- Located in `src/test/`

**UI/Smoke Tests:**
- Compose UI tests with `createAndroidComposeRule`
- Navigation verification
- Located in `src/androidTest/`

## Common Patterns

**Async Testing with Coroutines:**
```kotlin
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@Test
fun refreshRemoteRules_fetchesCurrentAndNextYear() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val repository = HolidayRulesRepository(
        context = context,
        applicationScope = backgroundScope,
        ioDispatcher = dispatcher,
        ...
    )
    advanceUntilIdle()
    
    val result = repository.refreshRemoteRules()
    advanceUntilIdle()
    
    assertEquals(HolidayRefreshResult.Updated, result)
}
```

**Robolectric Tests:**
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class HolidayRulesRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()
    
    // Test implementation...
}
```

**Error/Exception Testing:**
```kotlin
@Test
fun overtimeEntryValidator_rejectsCompTimeOnNonWorkday() {
    val result = OvertimeEntryValidator.validate(minutes = -30, resolvedDayType = DayType.REST_DAY)
    
    assertEquals("只有工作日才能申请调休", (result as DomainResult.Failure).message)
}
```

**Compose UI Testing:**
```kotlin
@RunWith(AndroidJUnit4::class)
class MainSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreenShowsSummaryAndCalendar() {
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("summary_card").assertIsDisplayed()
    }
}
```

---

*Testing analysis: 2026-03-15*
