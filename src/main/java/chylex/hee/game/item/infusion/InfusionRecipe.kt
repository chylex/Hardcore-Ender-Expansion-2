package chylex.hee.game.item.infusion
import net.minecraft.item.crafting.Ingredient

enum class InfusionRecipe(val infusion: Infusion, val rate: Int, val updates: Int, val ingredients: Array<Ingredient>){
}
