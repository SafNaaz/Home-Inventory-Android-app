package com.homeinventory.app.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class InventorySubcategory(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val category: InventoryCategory
) {
    // Fridge subcategories
    DOOR_BOTTLES("Door Bottles", Icons.Default.LocalDrink, Color(0xFF2196F3), InventoryCategory.FRIDGE),
    TRAY("Tray Section", Icons.Default.FoodBank, Color(0xFFFF9800), InventoryCategory.FRIDGE),
    MAIN("Main Section", Icons.Default.Kitchen, Color(0xFF4CAF50), InventoryCategory.FRIDGE),
    VEGETABLE("Vegetable Section", Icons.Default.LocalFlorist, Color(0xFF00BCD4), InventoryCategory.FRIDGE),
    FREEZER("Freezer", Icons.Default.AcUnit, Color(0xFF03A9F4), InventoryCategory.FRIDGE),
    MINI_COOLER("Mini Cooler", Icons.Default.Icecream, Color(0xFF9C27B0), InventoryCategory.FRIDGE),

    // Grocery subcategories
    RICE("Rice Items", Icons.Default.Fastfood, Color(0xFF795548), InventoryCategory.GROCERY),
    PULSES("Pulses", Icons.Default.Grass, Color(0xFFFFEB3B), InventoryCategory.GROCERY),
    CEREALS("Cereals", Icons.Default.Restaurant, Color(0xFFFF9800), InventoryCategory.GROCERY),
    CONDIMENTS("Condiments", Icons.Default.LocalDrink, Color(0xFFF44336), InventoryCategory.GROCERY),
    OILS("Oils", Icons.Default.WaterDrop, Color(0xFFFFEB3B), InventoryCategory.GROCERY),

    // Hygiene subcategories
    WASHING("Washing", Icons.Default.LocalLaundryService, Color(0xFF2196F3), InventoryCategory.HYGIENE),
    DISHWASHING("Dishwashing", Icons.Default.CleanHands, Color(0xFF4CAF50), InventoryCategory.HYGIENE),
    TOILET_CLEANING("Toilet Cleaning", Icons.Default.Sanitizer, Color(0xFF00BCD4), InventoryCategory.HYGIENE),
    KIDS("Kids", Icons.Default.ChildCare, Color(0xFFE91E63), InventoryCategory.HYGIENE),
    GENERAL_CLEANING("General Cleaning", Icons.Default.CleaningServices, Color(0xFF9C27B0), InventoryCategory.HYGIENE),

    // Personal Care subcategories
    FACE("Face", Icons.Default.Face, Color(0xFFE91E63), InventoryCategory.PERSONAL_CARE),
    BODY("Body", Icons.Default.Person, Color(0xFF00BCD4), InventoryCategory.PERSONAL_CARE),
    HEAD("Head", Icons.Default.FaceRetouchingNatural, Color(0xFF3F51B5), InventoryCategory.PERSONAL_CARE);

    companion object {
        fun fromString(value: String): InventorySubcategory = valueOf(value.uppercase())
        
        fun getSubcategoriesForCategory(category: InventoryCategory): List<InventorySubcategory> {
            return values().filter { it.category == category }
        }
    }

    fun getSampleItems(): List<String> {
        return when (this) {
            // Fridge sample items
            DOOR_BOTTLES -> listOf("Water Bottles", "Juice", "Milk", "Soft Drinks")
            TRAY -> listOf("Eggs", "Butter", "Cheese", "Yogurt")
            MAIN -> listOf("Leftovers", "Cooked Food", "Fruits", "Vegetables")
            VEGETABLE -> listOf("Onions", "Tomatoes", "Potatoes", "Leafy Greens")
            FREEZER -> listOf("Ice Cream", "Frozen Vegetables", "Meat", "Ice Cubes")
            MINI_COOLER -> listOf("Cold Drinks", "Snacks", "Chocolates")

            // Grocery sample items
            RICE -> listOf("Basmati Rice", "Brown Rice", "Jasmine Rice", "Wild Rice")
            PULSES -> listOf("Lentils", "Chickpeas", "Black Beans", "Kidney Beans")
            CEREALS -> listOf("Oats", "Cornflakes", "Wheat Flakes", "Muesli")
            CONDIMENTS -> listOf("Salt", "Sugar", "Spices", "Sauces")
            OILS -> listOf("Cooking Oil", "Olive Oil", "Coconut Oil", "Ghee")

            // Hygiene sample items
            WASHING -> listOf("Detergent", "Fabric Softener", "Stain Remover")
            DISHWASHING -> listOf("Dish Soap", "Dishwasher Tablets", "Sponges")
            TOILET_CLEANING -> listOf("Toilet Cleaner", "Toilet Paper", "Air Freshener")
            KIDS -> listOf("Diapers", "Baby Wipes", "Baby Shampoo")
            GENERAL_CLEANING -> listOf("All-Purpose Cleaner", "Floor Cleaner", "Glass Cleaner")

            // Personal Care sample items
            FACE -> listOf("CC Cream", "Powder", "Face Wash", "Moisturizer")
            BODY -> listOf("Lotion", "Deodorant", "Bathing Soap", "Body Wash")
            HEAD -> listOf("Shampoo", "Conditioner", "Hair Oil", "Hair Gel")
        }
    }
}
