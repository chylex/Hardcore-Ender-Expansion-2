package chylex.hee.game.item.infusion
import chylex.hee.game.item.infusion.InfusionRecipe.Ingredients.Companion.item
import chylex.hee.game.item.infusion.InfusionRecipe.Ingredients.Companion.tag
import chylex.hee.init.ModBlocks.ENDIUM_BLOCK
import chylex.hee.init.ModItems.ALTERATION_NEXUS
import chylex.hee.init.ModItems.AMELIOR
import chylex.hee.init.ModItems.AURICION
import chylex.hee.init.ModItems.DRAGON_SCALE
import chylex.hee.init.ModItems.ECTOPLASM
import chylex.hee.init.ModItems.ENCHANTED_CLAW
import chylex.hee.init.ModItems.ENDIUM_INGOT
import chylex.hee.init.ModItems.REVITALIZATION_SUBSTANCE
import chylex.hee.init.ModItems.VOID_ESSENCE
import net.minecraft.item.Item
import net.minecraft.item.Items.BLAZE_POWDER
import net.minecraft.item.Items.ENDER_PEARL
import net.minecraft.item.Items.FIRE_CHARGE
import net.minecraft.item.Items.FLINT
import net.minecraft.item.Items.GLOWSTONE_DUST
import net.minecraft.item.Items.LEAD
import net.minecraft.item.Items.REDSTONE_BLOCK
import net.minecraft.item.Items.REDSTONE_TORCH
import net.minecraft.item.Items.SCUTE
import net.minecraft.item.crafting.Ingredient
import net.minecraft.tags.ItemTags.WOOL
import net.minecraft.tags.Tag
import net.minecraft.util.IItemProvider
import net.minecraftforge.common.Tags.Items.FEATHERS
import net.minecraftforge.common.Tags.Items.GEMS_QUARTZ
import net.minecraftforge.common.Tags.Items.GUNPOWDER
import net.minecraftforge.common.Tags.Items.NUGGETS_IRON
import net.minecraftforge.common.Tags.Items.SLIMEBALLS
import net.minecraftforge.common.Tags.Items.STRING
import java.util.Collections

enum class InfusionRecipe(val infusion: Infusion, val rate: Int, val updates: Int, val ingredients: Array<out Ingredient>){
	POWER   (Infusion.POWER,    Rate(10), Updates(10), Ingredients(2 to tag(GUNPOWDER))),
	FIRE    (Infusion.FIRE,     Rate(10), Updates(10), Ingredients(item(FIRE_CHARGE), item(FLINT))),
	TRAP    (Infusion.TRAP,     Rate(10), Updates(10), Ingredients(item(REDSTONE_TORCH), tag(GEMS_QUARTZ))),
	HARMLESS(Infusion.HARMLESS, Rate(10), Updates(10), Ingredients(tag(WOOL), tag(SLIMEBALLS))),
	PHASING (Infusion.PHASING,  Rate(10), Updates(10), Ingredients(item(ENDER_PEARL), item(VOID_ESSENCE))),
	SLOW    (Infusion.SLOW,     Rate(10), Updates(10), Ingredients(tag(FEATHERS), item(SCUTE))),
	RIDING  (Infusion.RIDING,   Rate(10), Updates(10), Ingredients(item(LEAD), tag(NUGGETS_IRON))),
	
	VIGOR   (Infusion.VIGOR,    Rate(3), Updates(100), Ingredients(item(AMELIOR), item(ENCHANTED_CLAW), item(ENDIUM_INGOT), item(GLOWSTONE_DUST))),
	CAPACITY(Infusion.CAPACITY, Rate(5), Updates( 36), Ingredients(item(ENDIUM_BLOCK), item(ALTERATION_NEXUS))),
	DISTANCE(Infusion.DISTANCE, Rate(5), Updates( 36), Ingredients(item(DRAGON_SCALE), tag(STRING))),
	SPEED   (Infusion.SPEED,    Rate(5), Updates( 36), Ingredients(item(BLAZE_POWDER), item(REDSTONE_BLOCK))),
	
	STABILITY(Infusion.STABILITY, Rate(3), Updates(75), Ingredients(2 to item(AURICION), 1 to item(AMELIOR), 1 to item(ENDIUM_INGOT))),
	SAFETY   (Infusion.SAFETY,    Rate(3), Updates(75), Ingredients(3 to item(REVITALIZATION_SUBSTANCE), 1 to item(ECTOPLASM)));
	
	// TODO EXPANSION(Infusion.EXPANSION, Rate(2), Updates(160), Ingredients());
	
	// Construction helpers
	
	constructor(infusion: Infusion, rate: Rate, updates: Updates, ingredients: Ingredients) : this(infusion, rate.rate, updates.updates, ingredients.ingredients)
	
	private class Rate(val rate: Int)
	private class Updates(val updates: Int)
	
	private class Ingredients(vararg val ingredients: Ingredient){
		constructor(vararg ingredients: Pair<Int, Ingredient>) : this(*ingredients.flatMap { (amount, ingredient) -> Collections.nCopies(amount, ingredient) }.toTypedArray())
		
		@Suppress("NOTHING_TO_INLINE")
		companion object{
			inline fun tag(tag: Tag<Item>): Ingredient = Ingredient.fromTag(tag)
			inline fun item(item: IItemProvider): Ingredient = Ingredient.fromItems(item)
		}
	}
}
