package org.diego.stash.domain.model

enum class Category(val displayName: String) {
    PRODUCE("Produce"),
    DAIRY("Dairy"),
    MEAT_FISH("Meat & Fish"),
    FROZEN("Frozen"),
    BAKERY("Bakery"),
    PANTRY("Pantry"),
    BEVERAGES("Beverages"),
    SNACKS("Snacks"),
    OTHER("Other")
}

enum class StorageLocation(val displayName: String) {
    FRIDGE("Fridge"),
    FREEZER("Freezer"),
    PANTRY("Pantry"),
    CABINET("Cabinet"),
    COUNTER("Counter"),
    OTHER("Other")
}

enum class ExpiryType {
    BEST_BEFORE,
    USE_BY
}

enum class FoodItemStatus {
    ACTIVE,
    CONSUMED,
    DISCARDED
}

enum class FoodUnit(val displayName: String) {
    UNITS("units"),
    G("g"),
    KG("kg"),
    ML("ml"),
    L("L"),
    CUPS("cups"),
    SLICES("slices"),
    PORTIONS("portions")
}
