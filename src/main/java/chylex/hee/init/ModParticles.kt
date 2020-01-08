package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.client.util.MC
import chylex.hee.game.particle.ParticleBubbleCustom
import chylex.hee.game.particle.ParticleDeathFlowerHeal
import chylex.hee.game.particle.ParticleEnderGoo
import chylex.hee.game.particle.ParticleEnergyCluster
import chylex.hee.game.particle.ParticleEnergyClusterRevitalization
import chylex.hee.game.particle.ParticleEnergyTableDrain
import chylex.hee.game.particle.ParticleEnergyTransferToPedestal
import chylex.hee.game.particle.ParticleEnergyTransferToPlayer
import chylex.hee.game.particle.ParticleExperienceOrbFloating
import chylex.hee.game.particle.ParticleFadingSpot
import chylex.hee.game.particle.ParticleFlameCustom
import chylex.hee.game.particle.ParticleGlitter
import chylex.hee.game.particle.ParticleGrowingSpot
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.ParticleSpellCustom
import chylex.hee.game.particle.ParticleTeleport
import chylex.hee.game.particle.ParticleVoid
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.named
import net.minecraft.client.particle.IAnimatedSprite
import net.minecraft.client.particle.IParticleFactory
import net.minecraft.client.particle.ParticleManager.IParticleMetaFactory
import net.minecraft.particles.BasicParticleType
import net.minecraft.particles.ParticleType
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModParticles{
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<ParticleType<*>>){
		with(e.registry){
			register(ParticleBubbleCustom.makeType named "bubble")
			register(ParticleDeathFlowerHeal.makeType named "death_flower_heal")
			register(ParticleEnderGoo.makeType named "ender_goo")
			register(ParticleEnergyCluster.makeType named "energy_cluster")
			register(ParticleEnergyClusterRevitalization.makeType named "energy_cluster_revitalization")
			register(ParticleEnergyTableDrain.makeType named "energy_table_drain")
			register(ParticleEnergyTransferToPedestal.makeType named "energy_transfer_to_pedestal")
			register(ParticleEnergyTransferToPlayer.makeType named "energy_transfer_to_player")
			register(ParticleExperienceOrbFloating.makeType named "experience_orb_floating")
			register(ParticleFadingSpot.makeType named "fading_spot")
			register(ParticleFlameCustom.makeType named "flame")
			register(ParticleGlitter.makeType named "glitter")
			register(ParticleGrowingSpot.makeType named "growing_spot")
			register(ParticleSmokeCustom.makeType named "smoke")
			register(ParticleSpellCustom.makeType named "spell")
			register(ParticleTeleport.makeType named "teleport")
			register(ParticleVoid.makeType named "void")
		}
	}
	
	@Sided(Side.CLIENT)
	@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID, bus = MOD)
	object Client{
		private val factories = mutableListOf<Runnable>()
		
		fun addFactory(type: BasicParticleType, maker: IParticleMaker<*>, callback: (IAnimatedSprite) -> Unit){
			factories.add(Runnable {
				MC.particleManager.registerFactory(type, IParticleMetaFactory {
					callback(it)
					
					IParticleFactory<BasicParticleType> { _, world, posX, posY, posZ, motX, motY, motZ ->
						maker.create(world, posX, posY, posZ, motX, motY, motZ, null)
					}
				})
			})
		}
		
		@SubscribeEvent
		fun onRegisterFactories(e: ParticleFactoryRegisterEvent){
			for(factory in factories){
				factory.run()
			}
			
			factories.clear()
		}
	}
}
