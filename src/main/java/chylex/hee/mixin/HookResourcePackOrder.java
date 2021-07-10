package chylex.hee.mixin;
import chylex.hee.client.VanillaResourceOverrides;
import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(ResourcePackList.class)
public abstract class HookResourcePackOrder {
	@Inject(method = "func_232618_b_", at = @At("RETURN"), cancellable = true)
	private void patchOrder(final Collection<String> packs, final CallbackInfoReturnable<List<ResourcePackInfo>> ci) {
		final List<ResourcePackInfo> list = new ArrayList<>(ci.getReturnValue());
		
		for (int i = 0; i < list.size() - 1; i++) {
			final ResourcePackInfo pack = list.get(i);
			if (pack.isAlwaysEnabled() && pack.getName().equals(VanillaResourceOverrides.PACK_NAME)) {
				list.remove(i);
				list.add(pack);
				break;
			}
		}
		
		ci.setReturnValue(ImmutableList.copyOf(list));
	}
}
