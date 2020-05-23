package chylex.hee.init.factory;
import chylex.hee.game.block.entity.TileEntityAccumulationTable;
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom;
import chylex.hee.game.block.entity.TileEntityDarkChest;
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor;
import chylex.hee.game.block.entity.TileEntityEnergyCluster;
import chylex.hee.game.block.entity.TileEntityExperienceGate;
import chylex.hee.game.block.entity.TileEntityExperienceTable;
import chylex.hee.game.block.entity.TileEntityIgneousPlate;
import chylex.hee.game.block.entity.TileEntityInfusedTNT;
import chylex.hee.game.block.entity.TileEntityInfusionTable;
import chylex.hee.game.block.entity.TileEntityJarODust;
import chylex.hee.game.block.entity.TileEntityLootChest;
import chylex.hee.game.block.entity.TileEntityMinersBurialAltar;
import chylex.hee.game.block.entity.TileEntityPortalInner;
import chylex.hee.game.block.entity.TileEntityShulkerBoxCustom;
import chylex.hee.game.block.entity.TileEntitySpawnerObsidianTower;
import chylex.hee.game.block.entity.TileEntityTablePedestal;
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage;
import net.minecraft.tileentity.TileEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class TileEntityConstructors{
	private static final Map<Class<? extends TileEntity>, Supplier<? extends TileEntity>> all = new HashMap<>();
	
	static{
		add(TileEntityAccumulationTable.class, TileEntityAccumulationTable::new);
		add(TileEntityBrewingStandCustom.class, TileEntityBrewingStandCustom::new);
		add(TileEntityDarkChest.class, TileEntityDarkChest::new);
		add(TileEntityEndPortalAcceptor.class, TileEntityEndPortalAcceptor::new);
		add(TileEntityEnergyCluster.class, TileEntityEnergyCluster::new);
		add(TileEntityExperienceGate.class, TileEntityExperienceGate::new);
		add(TileEntityExperienceTable.class, TileEntityExperienceTable::new);
		add(TileEntityIgneousPlate.class, TileEntityIgneousPlate::new);
		add(TileEntityInfusedTNT.class, TileEntityInfusedTNT::new);
		add(TileEntityInfusionTable.class, TileEntityInfusionTable::new);
		add(TileEntityJarODust.class, TileEntityJarODust::new);
		add(TileEntityLootChest.class, TileEntityLootChest::new);
		add(TileEntityMinersBurialAltar.class, TileEntityMinersBurialAltar::new);
		add(TileEntityPortalInner.End.class, TileEntityPortalInner.End::new);
		add(TileEntityPortalInner.Void.class, TileEntityPortalInner.Void::new);
		add(TileEntityShulkerBoxCustom.class, TileEntityShulkerBoxCustom::new);
		add(TileEntitySpawnerObsidianTower.class, TileEntitySpawnerObsidianTower::new);
		add(TileEntityTablePedestal.class, TileEntityTablePedestal::new);
		add(TileEntityVoidPortalStorage.class, TileEntityVoidPortalStorage::new);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends TileEntity> Supplier<T> get(Class<T> cls){
		return (Supplier<T>)all.get(cls);
	}
	
	/**
	 * Ensures the class and constructor are compatible to catch typos.
	 */
	private static <T extends TileEntity> void add(Class<T> cls, Supplier<T> constructor){
		all.put(cls, constructor);
	}
	
	private TileEntityConstructors(){}
}
