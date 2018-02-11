package com.mmorpg.mir.model.artifact.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.h2.util.New;

import com.mmorpg.mir.log.LogManager;
import com.mmorpg.mir.model.artifact.core.ArtifactManager;
import com.mmorpg.mir.model.artifact.packet.SM_Artifact_Buff_Deprect;
import com.mmorpg.mir.model.artifact.packet.SM_Artifact_EnhanceCount_Change;
import com.mmorpg.mir.model.artifact.packet.SM_Artifact_Grow_Item_Change;
import com.mmorpg.mir.model.artifact.packet.SM_Artifact_Uplevel_Notify_Others;
import com.mmorpg.mir.model.artifact.resource.ArtifactGrowItemResource;
import com.mmorpg.mir.model.artifact.resource.ArtifactResource;
import com.mmorpg.mir.model.gameobjects.Player;
import com.mmorpg.mir.model.gameobjects.stats.Stat;
import com.mmorpg.mir.model.gameobjects.stats.StatEffectId;
import com.mmorpg.mir.model.gameobjects.stats.StatEffectType;
import com.mmorpg.mir.model.horse.manager.HorseManager;
import com.mmorpg.mir.model.item.core.ItemManager;
import com.mmorpg.mir.model.item.resource.ItemResource;
import com.mmorpg.mir.model.utils.PacketSendUtility;
import com.windforce.common.utility.DateUtils;

/**
 * 神兵
 * 
 * @author 37wan
 * 
 */
public class Artifact {

	public static final StatEffectId Artifact_STAT_ID = StatEffectId.valueOf("Artifact-pf", StatEffectType.ARTIFACT);
	public static final StatEffectId Artifact_BUFF_ID = StatEffectId.valueOf("Artifact-buff", StatEffectType.ARTIFACT);
	public static final StatEffectId ARTIFACT_ENHANCE = StatEffectId.valueOf("artifact_enhance",
			StatEffectType.ARTIFACT);
	public static final StatEffectId ARTIFACT_GROW = StatEffectId.valueOf("artifact_grow", StatEffectType.ARTIFACT);
	public static final StatEffectId ARTIFACT_BLESS = StatEffectId.valueOf("artifact_bless", StatEffectType.ARTIFACT);

	// 阶数
	private int level;
	// 等级
	private int rank;
	private long clearTime; // 清除祝福值的时间
	@Deprecated
	private int nowBlessValue; // 玩家当前的祝福值
	private int upSum; // 进阶的次数
	// 外观
	private int appLevel;
	/** 神兵伤害BUFF时间 */
	private volatile long buffEndTime;
	/** buff是否过期 */
	private volatile boolean buffDeprect;
	/** 强化道具使用次数 */
	private Map<String, Integer> enhanceItemCount;
	/** 成长丹 */
	private Map<String, Integer> growItemCount;

	@Transient
	private transient Player owner;

	public static Artifact valueOf(Player player) {
		Artifact self = new Artifact();
		self.growItemCount = new HashMap<String, Integer>();
		self.owner = player;
		self.level = 1;
		return self;
	}

	@JsonIgnore
	public void addGrowItemCount(String itemId) {
		if (!this.growItemCount.containsKey(itemId)) {
			this.growItemCount.put(itemId, 0);
		}
		this.growItemCount.put(itemId, this.growItemCount.get(itemId) + 1);
		owner.getGameStats().replaceModifiers(ARTIFACT_GROW, getAllGrowItemStats(), true);

		PacketSendUtility.sendPacket(owner, SM_Artifact_Grow_Item_Change.valueOf(this));
	}

	@JsonIgnore
	public Stat[] getAllGrowItemStats() {
		List<Stat> itemStats = new ArrayList<Stat>();
		for (Map.Entry<String, Integer> itemCountEntry : this.growItemCount.entrySet()) {
			String itemId = itemCountEntry.getKey();
			int count = itemCountEntry.getValue();
			ArtifactGrowItemResource resource = ArtifactManager.getInstance().artifactGrowItemStorage.get(itemId, true);
			for (int i = 0; i < count; i++) {
				itemStats.addAll(Arrays.asList(resource.getStat()));
			}
		}
		return itemStats.toArray(new Stat[0]);
	}

	@JsonIgnore
	public void updateAppLevel(int appLevel) {
		this.appLevel = 1;
		PacketSendUtility.broadcastPacket(owner, SM_Artifact_Uplevel_Notify_Others.valueOf(owner), true);
	}

	@JsonIgnore
	synchronized public void checkBuffDeprect() {
		if (!buffDeprect && buffEndTime < System.currentTimeMillis()) {
			buffDeprect = true;
			owner.getGameStats().endModifiers(Artifact.Artifact_BUFF_ID, true);
			PacketSendUtility.sendPacket(owner, new SM_Artifact_Buff_Deprect());
		}
	}

	@JsonIgnore
	public void addLevel() {
		this.level++;
		LogManager.artifactUp(owner.getPlayerEnt().getServer(), owner.getPlayerEnt().getAccountName(), owner.getName(),
				owner.getObjectId(), owner.getArtifact().getLevel(), owner.getArtifact().getRank(), owner.getArtifact()
						.getUpSum(), System.currentTimeMillis());
	}

	@JsonIgnore
	public void upgrade() {
		if (isMaxRank()) {
			this.level++;
			this.rank = 0;
		} else {
			this.rank++;
		}
		LogManager.artifactUp(owner.getPlayerEnt().getServer(), owner.getPlayerEnt().getAccountName(), owner.getName(),
				owner.getObjectId(), owner.getArtifact().getLevel(), owner.getArtifact().getRank(), owner.getArtifact()
						.getUpSum(), System.currentTimeMillis());
		clear();
	}

	@JsonIgnore
	public void clear() {
		this.upSum = 0;
		this.clearTime = 0L;
		this.nowBlessValue = 0;
	}

	@JsonIgnore
	public void addBless(int add) {
		nowBlessValue += add;
		long now = System.currentTimeMillis();
		if (getResource().isCountReset() && clearTime < now)
			clearTime = now + HorseManager.getInstance().getIntervalTime();
	}

	public void setUpSum(int upSum) {
		this.upSum = upSum;
	}

	@JsonIgnore
	public void addUpSum(int add) {
		upSum += add;
		long now = System.currentTimeMillis();
		if (getResource().isCountReset() && clearTime < now)
			clearTime = now + HorseManager.getInstance().getIntervalTime();
	}

	public int getUpSum() {
		return upSum;
	}

	@JsonIgnore
	public boolean isTimeOut(long interval) {
		long nowTime = System.currentTimeMillis();
		long surplusTimes = interval * DateUtils.MILLIS_PER_HOUR - (clearTime - nowTime);
		return surplusTimes <= 0;
	}

	@JsonIgnore
	public void refreshBlessing(Player player) {
		if (getResource().isCountReset() && clearTime <= System.currentTimeMillis()) {
			this.nowBlessValue = 0;
			upSum = 0;
			// PacketSendUtility.sendPacket(player,
			// this.createArtifactVO(player));
		}
	}

	@JsonIgnore
	synchronized public void addBuffTime(long time) {
		if (this.buffEndTime < System.currentTimeMillis()) {
			this.buffEndTime = System.currentTimeMillis() + time;
		} else {
			this.buffEndTime += time;
		}
		buffDeprect = false;
		Stat[] stats = ArtifactManager.getInstance().BUFF_STATS.getValue();
		owner.getGameStats().endModifiers(Artifact.Artifact_BUFF_ID, false);
		owner.getGameStats().addModifiers(Artifact.Artifact_BUFF_ID, stats, true);
	}

	public long getClearTime() {
		return clearTime;
	}

	public void setClearTime(long clearTime) {
		this.clearTime = clearTime;
	}

	public void intervalClearTime(long clearTime, long intervalTime) {
		this.clearTime = clearTime + intervalTime * DateUtils.MILLIS_PER_HOUR;
	}

	public int getLevel() {
		if (level < 1) {
			level = 1;
		}
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getNowBlessValue() {
		return nowBlessValue;
	}

	public void setNowBlessValue(int nowBlessValue) {
		this.nowBlessValue = nowBlessValue;
	}

	@JsonIgnore
	public Player getOwner() {
		return owner;
	}

	@JsonIgnore
	public void setOwner(Player owner) {
		this.owner = owner;
	}

	@JsonIgnore
	public ArtifactResource getResource() {
		return ArtifactManager.getInstance().getArtifactResource(level);
	}

	@JsonIgnore
	public boolean isMaxGrade() {
		return getResource().getCount() == 0 ? true : false;
	}

	@JsonIgnore
	public boolean isMaxRank() {
		int[] needCount = getResource().getNeedCount();
		return this.rank == needCount.length - 1;
	}

	@JsonIgnore
	public List<Stat> getArtifactStat() {
		List<Stat> stats = New.arrayList();
		ArtifactResource hr = ArtifactManager.getInstance().getArtifactResource(level);
		stats.addAll(Arrays.asList(hr.getStats()[this.rank]));
		return stats;
	}

	public int getAppLevel() {
		return appLevel;
	}

	public void setAppLevel(int appLevel) {
		this.appLevel = appLevel;
	}

	public long getBuffEndTime() {
		return buffEndTime;
	}

	public void setBuffEndTime(long buffEndTime) {
		this.buffEndTime = buffEndTime;
	}

	public boolean isBuffDeprect() {
		return buffDeprect;
	}

	public void setBuffDeprect(boolean buffDeprect) {
		this.buffDeprect = buffDeprect;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public Map<String, Integer> getEnhanceItemCount() {
		return enhanceItemCount;
	}

	public void setEnhanceItemCount(Map<String, Integer> enhanceItemCount) {
		this.enhanceItemCount = enhanceItemCount;
	}

	@JsonIgnore
	public void addEhanceStat(String itemCode) {
		if (!enhanceItemCount.containsKey(itemCode)) {
			enhanceItemCount.put(itemCode, 0);
		}
		enhanceItemCount.put(itemCode, enhanceItemCount.get(itemCode) + 1);
		owner.getGameStats().replaceModifiers(ARTIFACT_ENHANCE, getEnhanceStats(), true);
		PacketSendUtility.sendPacket(owner, SM_Artifact_EnhanceCount_Change.valueOf(owner));
	}

	@JsonIgnore
	public List<Stat> getEnhanceStats() {
		List<Stat> enhanceStat = new ArrayList<Stat>();
		for (Map.Entry<String, Integer> entry : enhanceItemCount.entrySet()) {
			ItemResource itemResource = ItemManager.getInstance().getItemResources().get(entry.getKey(), true);
			for (int i = 0; i < entry.getValue(); i++) {
				enhanceStat.addAll(Arrays.asList(itemResource.getStats()));
			}
		}
		return enhanceStat;
	}

	public Map<String, Integer> getGrowItemCount() {
		return growItemCount;
	}

	public void setGrowItemCount(Map<String, Integer> growItemCount) {
		this.growItemCount = growItemCount;
	}
	
	@JsonIgnore
	public boolean isMaxLevel() {
		int[] needCount = getResource().getNeedCount();
		return this.rank == needCount.length - 1;
	}

	@JsonIgnore
	public Stat[] getTempBlessStats() {
		ArtifactResource resource = getResource();
		if (upSum > 0 && !resource.isMaxGrade()) {
			Stat[] currentStats = resource.getStats()[this.rank];
			Stat[] nextLevelStats = null;
			if (isMaxLevel()) {
				nextLevelStats = ArtifactManager.getInstance().getArtifactResource(level + 1).getStats()[0];
			} else {
				nextLevelStats = resource.getStats()[this.rank + 1];
			}
			Stat[] tempStats = new Stat[nextLevelStats.length];
			for (int i = 0; i < nextLevelStats.length; i++) {
				Stat temp = nextLevelStats[i].copyOf();
				for (int j = 0; j < currentStats.length; j++) {
					Stat target = currentStats[j];
					if (temp.getType() == target.getType()) {
						long difA = temp.getValueA() - target.getValueA();
						long difB = temp.getValueB() - target.getValueB();
						long difC = temp.getValueC() - target.getValueC();
						if (difA >= 0) {
							temp.setValueA(difA);
						}
						if (difB >= 0) {
							temp.setValueB(difB);
						}
						if (difC >= 0) {
							temp.setValueC(difC);
						}
					}
				}
				tempStats[i] = temp;
			}
			
			double factor = upSum * 1.0 / resource.getNeedCount()[this.rank];
			for (Stat stat : tempStats) {
				stat.multipMerge(factor);
			}
			return tempStats;
		}
		return null;
	}
}