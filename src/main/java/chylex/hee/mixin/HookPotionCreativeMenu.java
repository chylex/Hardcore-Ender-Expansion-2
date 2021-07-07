package chylex.hee.mixin;
import chylex.hee.init.ModPotions;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PotionItem.class)
@SuppressWarnings("MethodMayBeStatic")
public abstract class HookPotionCreativeMenu {
	@Redirect(method = "fillItemGroup", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/NonNullList;add(Ljava/lang/Object;)Z"))
	private boolean onItemsListAdd(final NonNullList<ItemStack> items, final Object stack) {
		if (ModPotions.excludeFromCreativeMenu(PotionUtils.getPotionFromItem((ItemStack)stack))) {
			return false;
		}
		else {
			return items.add((ItemStack)stack);
		}
	}
}
