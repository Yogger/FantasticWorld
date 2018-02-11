package com.mmorpg.mir;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mmorpg.mir.model.ServerConfigValue;
import com.mmorpg.mir.model.SpringComponentStation;
import com.mmorpg.mir.model.addication.AntiAddictionManager;
import com.mmorpg.mir.model.assassin.manager.AssassinManager;
import com.mmorpg.mir.model.boss.manager.BossManager;
import com.mmorpg.mir.model.country.manager.CountryManager;
import com.mmorpg.mir.model.countrycopy.manager.CountryCopyManager;
import com.mmorpg.mir.model.exercise.ExerciseManager;
import com.mmorpg.mir.model.gascopy.manager.GasCopyManager;
import com.mmorpg.mir.model.kingofwar.manager.KingOfWarManager;
import com.mmorpg.mir.model.mergeactive.manager.MergeActiveManager;
import com.mmorpg.mir.model.ministerfete.manager.MinisterFeteManager;
import com.mmorpg.mir.model.monsterriot.manager.MonsterRiotManager;
import com.mmorpg.mir.model.quest.manager.QuestEventManager;
import com.mmorpg.mir.model.rank.manager.WorldRankManager;
import com.mmorpg.mir.model.relive.manager.PlayerReliveManager;
import com.mmorpg.mir.model.resourcecheck.ResourceCheck;
import com.mmorpg.mir.model.serverstate.ServerState;
import com.mmorpg.mir.model.session.FirewallFilter;
import com.mmorpg.mir.model.spawn.SpawnManager;
import com.mmorpg.mir.model.warship.manager.WarshipManager;
import com.windforce.common.utility.DateUtils;
import com.windforce.common.utility.JsonUtils;

public class Debug {

	private static final Logger logger = LoggerFactory.getLogger(Debug.class);

	public static boolean debug = false;
	//
	/** 默认的上下文配置名 */
	private static final String DEFAULT_APPLICATION_CONTEXT = "applicationContext.xml";

	public static void main(String[] arguments)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		ClassPathXmlApplicationContext applicationContext = null;
		long start = System.nanoTime();
		try {
			debug = true;
			applicationContext = new ClassPathXmlApplicationContext(DEFAULT_APPLICATION_CONTEXT);
			SpawnManager.getInstance().spawnAll();
			CountryManager.getInstance().initAll();
			BossManager.getInstance().initAll();
			ExerciseManager.getInstance().spawnAll();
			KingOfWarManager.getInstance().initSculptures();
			WarshipManager.getInstance().spawnAll();
			QuestEventManager.getInstance().reload();
			SpringComponentStation.getNicknameManager().reload();
			WorldRankManager.getInstance().initYesterDayRank();
			MonsterRiotManager.getInstance().initAll();
			ResourceCheck.instance.checkAllResource();
			CountryCopyManager.getInstance().initAll();
			PlayerReliveManager.getInstance().initAll();
			GasCopyManager.getInstance().initialGasCopyMapTask();
			MergeActiveManager.getInstance().initAll();
			ServerState.getInstance().initAll();
			AssassinManager.getInstance().initAll();
			MinisterFeteManager.getInstance().initAll();
			ServerConfigValue.versionMd5 = JsonUtils.object2String(arguments);
			System.gc();
			logger.error("容器已启动完成，开启服务器控制台");
			long end = System.nanoTime() - start;
			logger.error("防沉迷:" + AntiAddictionManager.openAnti + "，消耗了 " + TimeUnit.NANOSECONDS.toMillis(end)
					+ " ms (╯﹏╰)");
			FirewallFilter.setOpen(true);
		} catch (Exception exception) {
			exception.printStackTrace();
			logger.error("启动错误", exception);
			if (applicationContext != null) {
				if (applicationContext.isRunning()) {
					applicationContext.destroy();
				}
			}
			Runtime.getRuntime().exit(-1);
		}

		while (applicationContext.isActive()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("服务器主线程被非法打断", e);
				}
			}
		}

		while (applicationContext.isRunning()) {
			Thread.yield();
		}
		String message = MessageFormatter
				.format("服务器已经关闭 - [{}]", DateUtils.date2String(new Date(), DateUtils.PATTERN_DATE_TIME)).getMessage();
		logger.error(message);
		Runtime.getRuntime().exit(0);
	}

}