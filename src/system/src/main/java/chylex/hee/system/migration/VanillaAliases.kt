package chylex.hee.system.migration
import net.minecraft.enchantment.Enchantments
import net.minecraft.potion.Effect
import net.minecraft.potion.Effects
import net.minecraft.potion.Potion
import net.minecraft.potion.Potions
import net.minecraft.util.SoundEvents
import net.minecraft.util.math.BlockPos

typealias Sounds = SoundEvents
typealias Potion = Effect
typealias Potions = Effects
typealias PotionType = Potion
typealias PotionTypes = Potions
typealias Enchantments = Enchantments

typealias MutableBlockPos = BlockPos.Mutable

typealias BlockAbstractGlass = net.minecraft.block.AbstractGlassBlock
typealias BlockBed = net.minecraft.block.BedBlock
typealias BlockBrewingStand = net.minecraft.block.BrewingStandBlock
typealias BlockBush = net.minecraft.block.BushBlock
typealias BlockButton = net.minecraft.block.AbstractButtonBlock
typealias BlockCarpet = net.minecraft.block.CarpetBlock
typealias BlockCauldron = net.minecraft.block.CauldronBlock
typealias BlockChest = net.minecraft.block.ChestBlock
typealias BlockChorusFlower = net.minecraft.block.ChorusFlowerBlock
typealias BlockChorusPlant = net.minecraft.block.ChorusPlantBlock
typealias BlockDirectional = net.minecraft.block.DirectionalBlock
typealias BlockDispenser = net.minecraft.block.DispenserBlock
typealias BlockDoor = net.minecraft.block.DoorBlock
typealias BlockDoublePlant = net.minecraft.block.DoublePlantBlock
typealias BlockDragonEgg = net.minecraft.block.DragonEggBlock
typealias BlockEndPortal = net.minecraft.block.EndPortalBlock
typealias BlockFalling = net.minecraft.block.FallingBlock
typealias BlockFence = net.minecraft.block.FenceBlock
typealias BlockFire = net.minecraft.block.FireBlock
typealias BlockFlower = net.minecraft.block.FlowerBlock
typealias BlockFlowerPot = net.minecraft.block.FlowerPotBlock
typealias BlockFlowingFluid = net.minecraft.block.FlowingFluidBlock
typealias BlockFourWay = net.minecraft.block.FourWayBlock
typealias BlockFurnace = net.minecraft.block.FurnaceBlock
typealias BlockHorizontal = net.minecraft.block.HorizontalBlock
typealias BlockHorizontalFace = net.minecraft.block.HorizontalFaceBlock
typealias BlockLeaves = net.minecraft.block.LeavesBlock
typealias BlockMobSpawner = net.minecraft.block.SpawnerBlock
typealias BlockRedstoneWire = net.minecraft.block.RedstoneWireBlock
typealias BlockReed = net.minecraft.block.SugarCaneBlock
typealias BlockRotatedPillar = net.minecraft.block.RotatedPillarBlock
typealias BlockSapling = net.minecraft.block.SaplingBlock
typealias BlockShulkerBox = net.minecraft.block.ShulkerBoxBlock
typealias BlockSilverfish = net.minecraft.block.SilverfishBlock
typealias BlockSixWay = net.minecraft.block.SixWayBlock
typealias BlockSkull = net.minecraft.block.SkullBlock
typealias BlockSkullWall = net.minecraft.block.WallSkullBlock
typealias BlockSlab = net.minecraft.block.SlabBlock
typealias BlockStainedGlass = net.minecraft.block.StainedGlassBlock
typealias BlockStainedGlassPane = net.minecraft.block.StainedGlassPaneBlock
typealias BlockStairs = net.minecraft.block.StairsBlock
typealias BlockTNT = net.minecraft.block.TNTBlock
typealias BlockTorch = net.minecraft.block.TorchBlock
typealias BlockTrapDoor = net.minecraft.block.TrapDoorBlock
typealias BlockVine = net.minecraft.block.VineBlock
typealias BlockWall = net.minecraft.block.WallBlock
typealias BlockWeb = net.minecraft.block.WebBlock

typealias ItemArmor = net.minecraft.item.ArmorItem
typealias ItemAxe = net.minecraft.item.AxeItem
typealias ItemBlock = net.minecraft.item.BlockItem
typealias ItemBoneMeal = net.minecraft.item.BoneMealItem
typealias ItemBucket = net.minecraft.item.BucketItem
typealias ItemElytra = net.minecraft.item.ElytraItem
typealias ItemEnderEye = net.minecraft.item.EnderEyeItem
typealias ItemEnderPearl = net.minecraft.item.EnderPearlItem
typealias ItemExpBottle = net.minecraft.item.ExperienceBottleItem
typealias ItemPotion = net.minecraft.item.PotionItem
typealias ItemShears = net.minecraft.item.ShearsItem
typealias ItemSpawnEgg = net.minecraft.item.SpawnEggItem
typealias ItemSword = net.minecraft.item.SwordItem
typealias ItemTiered = net.minecraft.item.TieredItem
typealias ItemTool = net.minecraft.item.ToolItem
typealias ItemWallOrFloor = net.minecraft.item.WallOrFloorItem

typealias TileEntityBrewingStand = net.minecraft.tileentity.BrewingStandTileEntity
typealias TileEntityChest = net.minecraft.tileentity.ChestTileEntity
typealias TileEntityEndPortal = net.minecraft.tileentity.EndPortalTileEntity
typealias TileEntityFurnace = net.minecraft.tileentity.FurnaceTileEntity
typealias TileEntityLockableLoot = net.minecraft.tileentity.LockableLootTileEntity
typealias TileEntityShulkerBox = net.minecraft.tileentity.ShulkerBoxTileEntity
typealias TileEntitySkull = net.minecraft.tileentity.SkullTileEntity

typealias EntityAgeable = net.minecraft.entity.AgeableEntity
typealias EntityAmbientCreature = net.minecraft.entity.passive.AmbientEntity
typealias EntityAnimal = net.minecraft.entity.passive.AnimalEntity
typealias EntityArrow = net.minecraft.entity.projectile.AbstractArrowEntity
typealias EntityBat = net.minecraft.entity.passive.BatEntity
typealias EntityCat = net.minecraft.entity.passive.CatEntity
typealias EntityCreature = net.minecraft.entity.CreatureEntity
typealias EntityCreeper = net.minecraft.entity.monster.CreeperEntity
typealias EntityEnderCrystal = net.minecraft.entity.item.EnderCrystalEntity
typealias EntityEnderDragon = net.minecraft.entity.boss.dragon.EnderDragonEntity
typealias EntityEnderPearl = net.minecraft.entity.item.EnderPearlEntity
typealias EntityEnderman = net.minecraft.entity.monster.EndermanEntity
typealias EntityEndermite = net.minecraft.entity.monster.EndermiteEntity
typealias EntityFallingBlock = net.minecraft.entity.item.FallingBlockEntity
typealias EntityFlying = net.minecraft.entity.FlyingEntity
typealias EntityItem = net.minecraft.entity.item.ItemEntity
typealias EntityLightningBolt = net.minecraft.entity.effect.LightningBoltEntity
typealias EntityLiving = net.minecraft.entity.MobEntity
typealias EntityLivingBase = net.minecraft.entity.LivingEntity
typealias EntityLlamaSpit = net.minecraft.entity.projectile.LlamaSpitEntity
typealias EntityMob = net.minecraft.entity.monster.MonsterEntity
typealias EntityPlayer = net.minecraft.entity.player.PlayerEntity
typealias EntityPlayerMP = net.minecraft.entity.player.ServerPlayerEntity
typealias EntityPlayerSP = net.minecraft.client.entity.player.ClientPlayerEntity
typealias EntityPotion = net.minecraft.entity.projectile.PotionEntity
typealias EntitySilverfish = net.minecraft.entity.monster.SilverfishEntity
typealias EntitySnowball = net.minecraft.entity.projectile.SnowballEntity
typealias EntitySquid = net.minecraft.entity.passive.SquidEntity
typealias EntityTNTMinecart = net.minecraft.entity.item.minecart.TNTMinecartEntity
typealias EntityTNTPrimed = net.minecraft.entity.item.TNTEntity
typealias EntityTameable = net.minecraft.entity.passive.TameableEntity
typealias EntityThrowable = net.minecraft.entity.projectile.ThrowableEntity
typealias EntityVillager = net.minecraft.entity.merchant.villager.VillagerEntity
typealias EntityXPBottle = net.minecraft.entity.item.ExperienceBottleEntity
typealias EntityXPOrb = net.minecraft.entity.item.ExperienceOrbEntity
typealias EntityZombieVillager = net.minecraft.entity.monster.ZombieVillagerEntity
