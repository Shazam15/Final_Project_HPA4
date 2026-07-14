package com.utp.finalproject.ui

import androidx.annotation.DrawableRes
import com.utp.finalproject.R
import com.utp.finalproject.data.local.entity.PetEntity

object PetArtwork {
    @DrawableRes
    fun pet(type: String, mood: String): Int {
        return when (type) {
            PetEntity.TYPE_CAT -> cat(mood)
            PetEntity.TYPE_RABBIT -> rabbit(mood)
            else -> dog(mood)
        }
    }

    @DrawableRes
    fun reward(assetName: String): Int {
        return when (assetName) {
            "collar_blue" -> R.drawable.reward_collar_blue
            "hat_green" -> R.drawable.reward_hat_green
            "hero_cape" -> R.drawable.reward_hero_cape
            "gold_color" -> R.drawable.reward_gold_color
            "garden_bg" -> R.drawable.reward_garden_bg
            else -> 0
        }
    }

    private fun dog(mood: String): Int = when (mood) {
        PetEntity.MOOD_NEUTRAL -> R.drawable.pet_dog_neutral
        PetEntity.MOOD_SAD -> R.drawable.pet_dog_sad
        PetEntity.MOOD_SICK -> R.drawable.pet_dog_sick
        PetEntity.MOOD_DANGER -> R.drawable.pet_dog_danger
        else -> R.drawable.pet_dog_happy
    }

    private fun cat(mood: String): Int = when (mood) {
        PetEntity.MOOD_NEUTRAL -> R.drawable.pet_cat_neutral
        PetEntity.MOOD_SAD -> R.drawable.pet_cat_sad
        PetEntity.MOOD_SICK -> R.drawable.pet_cat_sick
        PetEntity.MOOD_DANGER -> R.drawable.pet_cat_danger
        else -> R.drawable.pet_cat_happy
    }

    private fun rabbit(mood: String): Int = when (mood) {
        PetEntity.MOOD_NEUTRAL -> R.drawable.pet_rabbit_neutral
        PetEntity.MOOD_SAD -> R.drawable.pet_rabbit_sad
        PetEntity.MOOD_SICK -> R.drawable.pet_rabbit_sick
        PetEntity.MOOD_DANGER -> R.drawable.pet_rabbit_danger
        else -> R.drawable.pet_rabbit_happy
    }
}
