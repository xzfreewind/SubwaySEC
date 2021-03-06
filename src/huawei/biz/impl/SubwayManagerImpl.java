package huawei.biz.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import Utils.DistenceUtil;
import Utils.PriceUtil;
import Utils.TimeUtil;

import huawei.biz.CardManager;
import huawei.biz.SubwayManager;
import huawei.exam.CardEnum;
import huawei.exam.ReturnCodeEnum;
import huawei.exam.SubwayException;
import huawei.model.Card;
import huawei.model.ConsumeRecord;
import huawei.model.InvalidCard;
import huawei.model.Subways;
import huawei.model.Subways.DistanceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Title: 待考生实现类takeSubway乘车函数，其它已实现功能函数不用关注
 * </p>
 *
 * <p>
 * Description: 车乘中心
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 *
 * <p>
 * Company:
 * </p>
 *
 * @author
 * @version 1.0 OperationCenter V100R002C20, 2015/9/7]
 */
public class SubwayManagerImpl implements SubwayManager {
	private static final String SUBWAY_NAME = "subwayName";
	private static final String FIRST_STATION = "firstStation";
	private static final String LAST_STATION = "lastStation";
	private DistenceUtil distenceUtil;
	private Subways subways = new Subways();
	private CardManager cardManager;

	public SubwayManagerImpl(CardManager cardManager) {
		this.cardManager = cardManager;
	}

	/**
	 * 乘车 -- 待考生实现
	 *
	 * @param cardId
	 *            the card id
	 * @param enterStation
	 *            the enter station
	 * @param enterTime
	 *            the enter time
	 * @param exitStation
	 *            the exit station
	 * @param exitTime
	 *            the exit time
	 * @return 卡当前状态
	 * @throws SubwayException
	 *             the subway exception
	 */
	@Override
	public Card takeSubway(String cardId, String enterStation, String enterTime, String exitStation, String exitTime)
			throws SubwayException {
		Card card = cardManager.queryCard(cardId);
		if(card.isHistoryCard()==true)
			throw new SubwayException(ReturnCodeEnum.E06,InvalidCard.getInstance());
		// check parameter valid
		if (!TimeUtil.checkValid(enterTime, exitTime))
			throw new SubwayException(ReturnCodeEnum.E05, InvalidCard.getInstance());

		ConsumeRecord record = new ConsumeRecord();
		record.setEnterStation(enterStation);
		record.setEnterTime(enterTime);
		record.setExitStation(exitStation);
		record.setExitTime(exitTime);
//		cardManager.addRecord(cardId,record);
		distenceUtil = new DistenceUtil(subways);
		int distence = distenceUtil.getdistence(enterStation, exitStation);

		int billing;
		if (distence > 0){
			billing = PriceUtil.getPriceByDistence(distence);
			if (PriceUtil.isDiscountTime(enterTime) && card.getCardType().equals(CardEnum.B)) {
				billing = (int) (billing * 0.8);
			}
			card = cardManager.consume(cardId, billing);
			record.setConsumeMoney(billing);
			cardManager.addRecord(cardId,record);

		}
		//distence == 0
		else {

			int minute = TimeUtil.getTime(enterTime, exitTime);
			int price = PriceUtil.getPriceByMinute(minute);

			if (minute <= 30) {
				if (card.getCardType().equals(CardEnum.A)) {
					card = cardManager.consume(cardId, card.getMoney());
					record.setConsumeMoney(card.getMoney());
					cardManager.addRecord(cardId,record);
				}
			}
			//minute > 30
			else{
				if(card.getCardType().equals(CardEnum.A)){
					card = cardManager.consume(cardId, Math.max(3, card.getMoney()));
					record.setConsumeMoney(Math.max(3, card.getMoney()));
					cardManager.addRecord(cardId,record);
				}
				else{
					card = cardManager.consume(cardId, 3);
					record.setConsumeMoney(3);
					cardManager.addRecord(cardId,record);
				}
			}
		}
		return card;
	}

	@Override
	public void manageSubways() {
		List<Map<String, String>> subwayMap = new ArrayList<Map<String, String>>();
		Map<String, String> oneSubway = Maps.newHashMap();
		oneSubway.put(FIRST_STATION, "S0");
		oneSubway.put(LAST_STATION, "S8");
		oneSubway.put(SUBWAY_NAME, "0");
		subwayMap.add(oneSubway);

		Map<String, String> secondSubway = Maps.newHashMap();
		secondSubway.put(FIRST_STATION, "S10");
		secondSubway.put(LAST_STATION, "S18");
		secondSubway.put(SUBWAY_NAME, "1");
		subwayMap.add(secondSubway);

		Map<String, String> thirdSubway = Maps.newHashMap();
		thirdSubway.put(FIRST_STATION, "S20");
		thirdSubway.put(LAST_STATION, "S28");
		thirdSubway.put(SUBWAY_NAME, "2");
		subwayMap.add(thirdSubway);

		Map<String, String> forthSubway = Maps.newHashMap();
		forthSubway.put(FIRST_STATION, "S30");
		forthSubway.put(LAST_STATION, "S38");
		forthSubway.put(SUBWAY_NAME, "3");
		subwayMap.add(forthSubway);

		subways.setSubwayInfo(subwayMap);
		subways.setStationDistances(initStationsTable());
	}

	@Override
	public Subways querySubways() {
		return subways;
	}

	private Table<String, String, Subways.DistanceInfo> initStationsTable() {
		Table<String, String, Subways.DistanceInfo> distanceTable = HashBasedTable.create();
		// 0号线
		distanceTable.put("S0", "S1", new Subways.DistanceInfo("0", 800));
		distanceTable.put("S1", "S2", new Subways.DistanceInfo("0", 2168));
		distanceTable.put("S2", "S3", new Subways.DistanceInfo("0", 2098));
		distanceTable.put("S3", "S4", new Subways.DistanceInfo("0", 2165));
		distanceTable.put("S4", "S5", new Subways.DistanceInfo("0", 1500));
		distanceTable.put("S5", "S6", new Subways.DistanceInfo("0", 1345));
		distanceTable.put("S6", "S7", new Subways.DistanceInfo("0", 1567));
		distanceTable.put("S7", "S8", new Subways.DistanceInfo("0", 1897));

		distanceTable.put("S1", "S0", new Subways.DistanceInfo("0", 800));
		distanceTable.put("S2", "S1", new Subways.DistanceInfo("0", 2168));
		distanceTable.put("S3", "S2", new Subways.DistanceInfo("0", 2098));
		distanceTable.put("S4", "S3", new Subways.DistanceInfo("0", 2165));
		distanceTable.put("S5", "S4", new Subways.DistanceInfo("0", 1500));
		distanceTable.put("S6", "S5", new Subways.DistanceInfo("0", 1345));
		distanceTable.put("S7", "S6", new Subways.DistanceInfo("0", 1567));
		distanceTable.put("S8", "S7", new Subways.DistanceInfo("0", 1897));

		// 1号线
		distanceTable.put("S10", "S11", new Subways.DistanceInfo("1", 900));
		distanceTable.put("S11", "S12", new Subways.DistanceInfo("1", 1168));
		distanceTable.put("S12", "S5", new Subways.DistanceInfo("1", 2198));
		distanceTable.put("S5", "S14", new Subways.DistanceInfo("1", 2000));
		distanceTable.put("S14", "S15", new Subways.DistanceInfo("1", 1600));
		distanceTable.put("S15", "S16", new Subways.DistanceInfo("1", 1485));
		distanceTable.put("S16", "S17", new Subways.DistanceInfo("1", 1600));
		distanceTable.put("S17", "S18", new Subways.DistanceInfo("1", 1888));

		distanceTable.put("S11", "S10", new Subways.DistanceInfo("1", 900));
		distanceTable.put("S12", "S11", new Subways.DistanceInfo("1", 1168));
		distanceTable.put("S5", "S12", new Subways.DistanceInfo("1", 2198));
		distanceTable.put("S14", "S5", new Subways.DistanceInfo("1", 2000));
		distanceTable.put("S15", "S14", new Subways.DistanceInfo("1", 1600));
		distanceTable.put("S16", "S15", new Subways.DistanceInfo("1", 1485));
		distanceTable.put("S17", "S16", new Subways.DistanceInfo("1", 1600));
		distanceTable.put("S18", "S17", new Subways.DistanceInfo("1", 1888));

		// 2号线
		distanceTable.put("S20", "S21", new Subways.DistanceInfo("2", 1100));
		distanceTable.put("S21", "S22", new Subways.DistanceInfo("2", 1238));
		distanceTable.put("S22", "S23", new Subways.DistanceInfo("2", 1998));
		distanceTable.put("S23", "S15", new Subways.DistanceInfo("2", 1800));
		distanceTable.put("S15", "S25", new Subways.DistanceInfo("2", 1700));
		distanceTable.put("S25", "S26", new Subways.DistanceInfo("2", 1585));
		distanceTable.put("S26", "S27", new Subways.DistanceInfo("2", 1405));
		distanceTable.put("S27", "S28", new Subways.DistanceInfo("2", 1822));

		distanceTable.put("S21", "S20", new Subways.DistanceInfo("2", 1100));
		distanceTable.put("S22", "S21", new Subways.DistanceInfo("2", 1238));
		distanceTable.put("S23", "S22", new Subways.DistanceInfo("2", 1998));
		distanceTable.put("S15", "S23", new Subways.DistanceInfo("2", 1800));
		distanceTable.put("S25", "S15", new Subways.DistanceInfo("2", 1700));
		distanceTable.put("S26", "S25", new Subways.DistanceInfo("2", 1585));
		distanceTable.put("S27", "S26", new Subways.DistanceInfo("2", 1405));
		distanceTable.put("S28", "S27", new Subways.DistanceInfo("2", 1822));

		// 3号线
		distanceTable.put("S30", "S31", new Subways.DistanceInfo("3", 1110));
		distanceTable.put("S31", "S32", new Subways.DistanceInfo("3", 1338));
		distanceTable.put("S32", "S2", new Subways.DistanceInfo("3", 1568));
		distanceTable.put("S2", "S22", new Subways.DistanceInfo("3", 1450));
		distanceTable.put("S22", "S35", new Subways.DistanceInfo("3", 1680));
		distanceTable.put("S35", "S36", new Subways.DistanceInfo("3", 1345));
		distanceTable.put("S36", "S37", new Subways.DistanceInfo("3", 1555));
		distanceTable.put("S37", "S38", new Subways.DistanceInfo("3", 1682));

		distanceTable.put("S31", "S30", new Subways.DistanceInfo("3", 1110));
		distanceTable.put("S32", "S31", new Subways.DistanceInfo("3", 1338));
		distanceTable.put("S2", "S32", new Subways.DistanceInfo("3", 1568));
		distanceTable.put("S22", "S2", new Subways.DistanceInfo("3", 1450));
		distanceTable.put("S35", "S22", new Subways.DistanceInfo("3", 1680));
		distanceTable.put("S36", "S35", new Subways.DistanceInfo("3", 1345));
		distanceTable.put("S37", "S36", new Subways.DistanceInfo("3", 1555));
		distanceTable.put("S38", "S37", new Subways.DistanceInfo("3", 1682));
		return distanceTable;
	}
}