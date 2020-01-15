package chylex.hee.init.factory;
import chylex.hee.game.container.ContainerAmuletOfRecovery;
import chylex.hee.game.container.ContainerBrewingStandCustom;
import chylex.hee.game.container.ContainerLootChest;
import chylex.hee.game.container.ContainerPortalTokenStorage;
import chylex.hee.game.container.ContainerShulkerBoxInInventory;
import chylex.hee.game.container.ContainerTrinketPouch;
import net.minecraft.inventory.container.Container;
import net.minecraftforge.fml.network.IContainerFactory;
import java.util.HashMap;
import java.util.Map;

public final class ContainerConstructors{
	private static final Map<Class<? extends Container>, IContainerFactory<?>> all = new HashMap<>();
	
	static{
		add(ContainerAmuletOfRecovery.class, ContainerAmuletOfRecovery::new);
		add(ContainerBrewingStandCustom.class, ContainerBrewingStandCustom::new);
		add(ContainerLootChest.class, ContainerLootChest::new);
		add(ContainerPortalTokenStorage.class, ContainerPortalTokenStorage::new);
		add(ContainerShulkerBoxInInventory.class, ContainerShulkerBoxInInventory::new);
		add(ContainerTrinketPouch.class, ContainerTrinketPouch::new);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Container> IContainerFactory<T> get(Class<T> cls){
		return (IContainerFactory<T>)all.get(cls);
	}
	
	/**
	 * Ensures the class and constructor are compatible to catch typos.
	 */
	private static <T extends Container> void add(Class<T> cls, IContainerFactory<T> constructor){
		all.put(cls, constructor);
	}
	
	private ContainerConstructors(){}
}
