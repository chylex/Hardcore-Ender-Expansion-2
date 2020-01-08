package chylex.hee.init.factory;
import chylex.hee.client.gui.GuiAmuletOfRecovery;
import chylex.hee.client.gui.GuiBrewingStandCustom;
import chylex.hee.client.gui.GuiLootChest;
import chylex.hee.client.gui.GuiPortalTokenStorage;
import chylex.hee.client.gui.GuiTrinketPouch;
import net.minecraft.client.gui.ScreenManager.IScreenFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.ShulkerBoxScreen;
import net.minecraft.inventory.container.Container;
import java.util.HashMap;
import java.util.Map;

public final class ScreenConstructors{
	private static final Map<Class<? extends Screen>, IScreenFactory<?, ?>> all = new HashMap<>();
	
	static{
		add(GuiAmuletOfRecovery.class, GuiAmuletOfRecovery::new);
		add(GuiBrewingStandCustom.class, GuiBrewingStandCustom::new);
		add(GuiLootChest.class, GuiLootChest::new);
		add(GuiPortalTokenStorage.class, GuiPortalTokenStorage::new);
		add(ShulkerBoxScreen.class, ShulkerBoxScreen::new);
		add(GuiTrinketPouch.class, GuiTrinketPouch::new);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Container, U extends ContainerScreen<T>> IScreenFactory<T, U> get(Class<U> cls){
		return (IScreenFactory<T, U>)all.get(cls);
	}
	
	/**
	 * Ensures the class and constructor are compatible to catch typos.
	 */
	private static <T extends Container, U extends ContainerScreen<T>> void add(Class<U> cls, IScreenFactory<T, U> constructor){
		all.put(cls, constructor);
	}
	
	private ScreenConstructors(){}
}
