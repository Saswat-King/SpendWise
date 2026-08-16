package com.kingstudio.spendwise.ui.common
import android.graphics.Color
import com.kingstudio.spendwise.R
import androidx.core.graphics.toColorInt

object CategoryIconResolver {
    fun getIconResource(iconKey: String): Int = when (iconKey) {
        "ic_shopping_bag" -> R.drawable.ic_shopping_bag
        "ic_food" -> R.drawable.ic_food
        "ic_transport" -> R.drawable.ic_transport
        "ic_bills" -> R.drawable.ic_bills
        "ic_entertainment" -> R.drawable.ic_entertainment
        "ic_travel" -> R.drawable.ic_travel
        "ic_groceries" -> R.drawable.ic_groceries
        "ic_health" -> R.drawable.ic_health
        "ic_education" -> R.drawable.ic_education
        "ic_fitness" -> R.drawable.ic_fitness
        "ic_pets" -> R.drawable.ic_pets
        "ic_gifts" -> R.drawable.ic_gift
        "ic_subscriptions" -> R.drawable.ic_subscriptions
        "ic_other" -> R.drawable.ic_other
        else -> R.drawable.ic_other
    }


    fun getIconColor(hexColor: String): Int {
        return try {
            hexColor.toColorInt()
        } catch (e: IllegalArgumentException) {
            Color.GRAY
        }
    }

    fun getLightBackgroundColor(hexColor: String): Int {
        return try {
            val color = hexColor.toColorInt()
            Color.argb(38,Color.red(color), Color.green(color), Color.blue(color))
        } catch (e: IllegalArgumentException) {
            "#F0F0F0".toColorInt()
        }
    }
}