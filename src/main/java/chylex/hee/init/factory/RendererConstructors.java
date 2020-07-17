package chylex.hee.init.factory;
import chylex.hee.client.render.block.RenderTileDarkChest;
import chylex.hee.client.render.block.RenderTileEndPortal;
import chylex.hee.client.render.block.RenderTileExperienceGate;
import chylex.hee.client.render.block.RenderTileIgneousPlate;
import chylex.hee.client.render.block.RenderTileJarODust;
import chylex.hee.client.render.block.RenderTileLootChest;
import chylex.hee.client.render.block.RenderTileMinersBurialAltar;
import chylex.hee.client.render.block.RenderTileSpawner;
import chylex.hee.client.render.block.RenderTileTable;
import chylex.hee.client.render.block.RenderTileTablePedestal;
import chylex.hee.client.render.block.RenderTileVoidPortal;
import chylex.hee.client.render.entity.RenderEntityBossEnderEye;
import chylex.hee.client.render.entity.RenderEntityItem;
import chylex.hee.client.render.entity.RenderEntityItemNoBob;
import chylex.hee.client.render.entity.RenderEntityMobAbstractEnderman;
import chylex.hee.client.render.entity.RenderEntityMobAngryEnderman;
import chylex.hee.client.render.entity.RenderEntityMobBlobby;
import chylex.hee.client.render.entity.RenderEntityMobSpiderling;
import chylex.hee.client.render.entity.RenderEntityMobUndread;
import chylex.hee.client.render.entity.RenderEntityMobVampireBat;
import chylex.hee.client.render.entity.RenderEntityMobVillagerDying;
import chylex.hee.client.render.entity.RenderEntityNothing;
import chylex.hee.client.render.entity.RenderEntityProjectileEyeOfEnder;
import chylex.hee.client.render.entity.RenderEntitySprite;
import chylex.hee.client.render.entity.RenderEntityTokenHolder;
import net.minecraft.client.renderer.entity.EndermiteRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.entity.SilverfishRenderer;
import net.minecraft.client.renderer.entity.TNTRenderer;
import net.minecraft.client.renderer.entity.model.ShulkerModel;
import net.minecraft.client.renderer.tileentity.ShulkerBoxTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings({"unchecked", "RedundantCast", "rawtypes"})
public final class RendererConstructors{
	private static final Map<Class<? extends EntityRenderer<?>>, IRenderFactory> entities = new HashMap<>();
	private static final Map<Class<? extends TileEntityRenderer<?>>, Function<TileEntityRendererDispatcher, TileEntityRenderer>> tiles = new HashMap<>();
	
	static{
		entities.put((Class<? extends EntityRenderer<?>>)(Object)RenderEntitySprite.class, RenderEntitySprite::new);
		entities.put(EndermiteRenderer.class, EndermiteRenderer::new);
		entities.put(FallingBlockRenderer.class, FallingBlockRenderer::new);
		entities.put(RenderEntityBossEnderEye.class, RenderEntityBossEnderEye::new);
		entities.put(RenderEntityItem.class, RenderEntityItem::new);
		entities.put(RenderEntityItemNoBob.class, RenderEntityItemNoBob::new);
		entities.put(RenderEntityMobAbstractEnderman.class, RenderEntityMobAbstractEnderman::new);
		entities.put(RenderEntityMobAngryEnderman.class, RenderEntityMobAngryEnderman::new);
		entities.put(RenderEntityMobBlobby.class, RenderEntityMobBlobby::new);
		entities.put(RenderEntityMobSpiderling.class, RenderEntityMobSpiderling::new);
		entities.put(RenderEntityMobUndread.class, RenderEntityMobUndread::new);
		entities.put(RenderEntityMobVampireBat.class, RenderEntityMobVampireBat::new);
		entities.put(RenderEntityMobVillagerDying.class, RenderEntityMobVillagerDying::new);
		entities.put(RenderEntityNothing.class, RenderEntityNothing::new);
		entities.put(RenderEntityProjectileEyeOfEnder.class, RenderEntityProjectileEyeOfEnder::new);
		entities.put(RenderEntityTokenHolder.class, RenderEntityTokenHolder::new);
		entities.put(SilverfishRenderer.class, SilverfishRenderer::new);
		entities.put(TNTRenderer.class, TNTRenderer::new);
		
		tiles.put(RenderTileDarkChest.class, RenderTileDarkChest::new);
		tiles.put(RenderTileEndPortal.class, RenderTileEndPortal::new);
		tiles.put(RenderTileExperienceGate.class, RenderTileExperienceGate::new);
		tiles.put(RenderTileIgneousPlate.class, RenderTileIgneousPlate::new);
		tiles.put(RenderTileJarODust.class, RenderTileJarODust::new);
		tiles.put(RenderTileLootChest.class, RenderTileLootChest::new);
		tiles.put(RenderTileMinersBurialAltar.class, RenderTileMinersBurialAltar::new);
		tiles.put(RenderTileSpawner.class, RenderTileSpawner::new);
		tiles.put(RenderTileTable.class, RenderTileTable::new);
		tiles.put(RenderTileTablePedestal.class, RenderTileTablePedestal::new);
		tiles.put(RenderTileVoidPortal.class, RenderTileVoidPortal::new);
		tiles.put(ShulkerBoxTileEntityRenderer.class, dispatcher -> new ShulkerBoxTileEntityRenderer(new ShulkerModel(), dispatcher));
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T extends Entity, R extends EntityRenderer<? super T>> IRenderFactory getEntity(Class<R> cls){
		return (IRenderFactory<T>)entities.get(cls);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends TileEntity, R extends TileEntityRenderer<? super T>> Function<? super TileEntityRendererDispatcher, ? extends TileEntityRenderer<? super T>> getTile(Class<R> cls){
		return (Function<? super TileEntityRendererDispatcher, ? extends TileEntityRenderer<? super T>>)(Object)tiles.get(cls);
	}
	
	private RendererConstructors(){}
}
