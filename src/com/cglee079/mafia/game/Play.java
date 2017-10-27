package com.cglee079.mafia.game;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.cglee079.mafia.log.Logger;
import com.cglee079.mafia.model.UserInfo;
import com.cglee079.mafia.model.UserManager;

public class Play {
	private final static int MINUSER = 1;
	private final static int MAXUSER = 8;

	private String state 	= "";
	private String when 	= "";
	private boolean wantnext = false;

	private HashMap<Integer, Integer[]> chractorOfUserSize; // 참여 인원 숫자별 직업수
	private HashMap<String, Integer> 	numOfChractor; // 직업별 인원 배정
	private HashMap<String, String> 	userVote; // 유저 투표
	private HashMap<String, String> 	mafiaChoice; // 마피아가 선택한 인원
	
	private String copChoice 	= "";
	private String doctorChoice = "";

	private UserManager userManager;

	public Play(){
		chractorOfUserSize 	= new HashMap<>();
		numOfChractor 		= new HashMap<>();
		userVote			= new HashMap<>();
		mafiaChoice 		= new HashMap<>();

		chractorOfUserSize.put(1, new Integer[] { 1, 0, 0, 0 });
		chractorOfUserSize.put(2, new Integer[] { 1, 1, 0, 0 });
		chractorOfUserSize.put(3, new Integer[] { 1, 1, 0, 1 });
		chractorOfUserSize.put(4, new Integer[] { 1, 1, 1, 1 });
		chractorOfUserSize.put(5, new Integer[] { 2, 1, 1, 1 });
		chractorOfUserSize.put(6, new Integer[] { 2, 1, 1, 2 });
		chractorOfUserSize.put(7, new Integer[] { 2, 1, 1, 3 });
		chractorOfUserSize.put(8, new Integer[] { 3, 1, 1, 3 });
	}
	
	public Play(UserManager userManager) {
		this();
		this.userManager = userManager;
	}

	public boolean isInsizeUserNum() {
		int size = userManager.size();
		if (size >= MINUSER && size <= MAXUSER) { return true; }
		return false;
	}

	public boolean updateCharacter() {
		Integer numberOfUsers = userManager.size();
		Integer[] characterDivision = chractorOfUserSize.get(numberOfUsers);

		int numOfMafias = 0;
		int numOfCops 	= 0;
		int numOfDoctors= 0;
		int numOfCivils = 0;

		int maxOfMafias = characterDivision[0];
		int maxOfCops 	= characterDivision[1];
		int maxOfDoctors= characterDivision[2];
		int maxOfCivils = characterDivision[3];

		for (int i = 0; i < numberOfUsers; i++) {
			UserInfo user = userManager.getUser(i);

			/* 시연용 인원배정 */
			if (numberOfUsers == 4) {
				if (user.getName().equals("left") || user.getName().equals("찬구")) {
					user.setCharacter("MAFIA");
					numOfMafias++;
				}

				else if (user.getName().equals("center")) {
					user.setCharacter("COP");
					numOfCops++;
				}

				else if (user.getName().equals("right")) {
					user.setCharacter("DOCTOR");
					numOfDoctors++;
				}

				else {
					user.setCharacter("CIVIL");
					numOfCivils++;
				}
			}

			/* 실제 게임 인원 배정 */
			if (numberOfUsers != 4) {
				while (true) {
					int random = (int) (Math.random() * 4);
					if (random == 0 && numOfMafias < maxOfMafias) {
						user.setCharacter("MAFIA");
						numOfMafias++;
						break;
					}

					else if (random == 1 && numOfCops < maxOfCops) {
						user.setCharacter("COP");
						numOfCops++;
						break;
					}

					else if (random == 2 && numOfDoctors < maxOfDoctors) {
						user.setCharacter("DOCTOR");
						numOfDoctors++;
						break;
					}

					else if (random == 3 && numOfCivils < maxOfCivils) {
						user.setCharacter("CIVIL");
						numOfCivils++;
						break;
					}
				}
			}
		}
		
		numOfChractor.put("MAFIA", numOfMafias);
		numOfChractor.put("COP", numOfCops);
		numOfChractor.put("DOCTOR", numOfDoctors);
		numOfChractor.put("CIVIL", numOfCivils);

		return true;

	}

	public void initVote() {
		userVote.clear();
		String[] aliveUsername = userManager.getAliveUserNames();
		int length = aliveUsername.length;
		for (int i = 0; i < length; i++){
			userVote.put(aliveUsername[i], "");
		}

		Logger.i("새로운 투표가 시작되었습니다 . 투표가능 (생존) 유저 " + length + "명" + "\n");
	}

	public void initMafiaChoice() {
		mafiaChoice.clear();
		
		String[] aliveUsername = userManager.getAliveUserNames();
		int length = aliveUsername.length;
		for (int i = 0; i < length; i++){
			if (userManager.getUser(aliveUsername[i]).getCharacter().equals("MAFIA")){
				mafiaChoice.put(aliveUsername[i], "");
			}
		}
	}

	public void updateVote(String name, String choice) {
		userVote.put(name, choice);
	}

	public void updateMafiaChoice(String name, String choice) {
		mafiaChoice.put(name, choice);
	}

	public void updateCopChoice(String name, String choice) {
		copChoice = choice;
	}

	public void updateDoctorChoice(String name, String choice) {
		doctorChoice = choice;
	}

	public boolean isAllUserVote() {
		boolean result 			= true;
		Set<String> set 		= userVote.keySet();
		Iterator<String> iter 	= set.iterator();
		
		Logger.i("--------------투표 중간결과 -----------------------\n");
		while (iter.hasNext()) {
			String name = iter.next();

			if (userVote.get(name).equals("")) {
				Logger.i(name + " 님은   " + "아직 투표를 하지 않았습니다!" + "\n");
				result = false;
			} else
				Logger.i(name + " 님은   " + userVote.get(name) + " 님께 투표하였습니다!" + "\n");
		}

		return result;
	}

	public boolean isAllChracterChoice() {

		if (numOfChractor.get("COP") != 0){
			if (copChoice.equals("")){return false;}
		}
		
		if (numOfChractor.get("DOCTOR") != 0){
			if (doctorChoice.equals("")){ return false;}
		}
		
		Iterator iter = mafiaChoice.keySet().iterator();
		while (iter.hasNext()) {
			if (mafiaChoice.get(iter.next()).equals(""))
				return false;
		}

		return true;
	}

	public boolean isAliveCop() {
		return numOfChractor.get("COP") != 0;
	}

	public boolean isAliveDoctor() {
		return numOfChractor.get("DOCTOR") != 0;
	}

	public String getDiedUserByVote() {
		HashMap<String, Integer> votedUser = new HashMap<>();
		String name	= null;
		String voted= null;
		String maxUser = null;
		int maxCnt = 0;	
		int cnt = 0;
		
		Set<String> set = userVote.keySet();
		Iterator<String> iter = set.iterator();
		
		/* 유저, 뽑혀진 숫자 */
		while (iter.hasNext()) {
			name	= iter.next();
			voted	= userVote.get(name);
			
			if (votedUser.get(voted) != null) {
				cnt = votedUser.get(voted);
				votedUser.put(voted, cnt + 1);
			} else{
				votedUser.put(voted, 1);
			}
		}

		/* 가장 많이 뽑혀진 저를 찾음 */
		Set<String> set2 = votedUser.keySet();
		Iterator<String> iter2 = set2.iterator();
		
		while (iter2.hasNext()) {
			name = iter2.next();
			cnt = votedUser.get(name);
			Logger.i(name + " :   " + cnt + "\n");
			if (cnt > maxCnt) {
				maxCnt 	= cnt;
				maxUser = name;
			}
		}

		return maxUser;
	}

	public String getMaxMafiaChoice() {
		HashMap<String, Integer> choicedUsers = new HashMap<>();
		String name;
		String choiced;
		String maxuser = null;
		int maxint = 0;
		int cnt = 0;
		
		Set<String> set = mafiaChoice.keySet();
		Iterator<String> iter = set.iterator();

		/* 유저, 뽑혀진 숫자 */
		while (iter.hasNext()) {
			name = iter.next();
			choiced = mafiaChoice.get(name);
			if (choicedUsers.get(choiced) != null) {
				cnt = choicedUsers.get(choiced);
				choicedUsers.put(choiced, cnt + 1);
			} else {
				choicedUsers.put(choiced, 1);
			}
		}

		/* 가장 많이 뽑혀진 저를 찾음 */
		Set<String> set2 = choicedUsers.keySet();
		Iterator<String> iter2 = set2.iterator();
		
		while (iter2.hasNext()) {
			name = iter2.next();
			cnt = choicedUsers.get(name);
			Logger.i(name + cnt + "\n");
			if (cnt > maxint) {
				maxint = cnt;
				maxuser = name;
			}
		}

		return maxuser;
	}

	public int getNumberOfChracter(String character) {
		return numOfChractor.get(character);
	}

	public void setDied(String dieUser) {
		/* 가장 많이 투표된 유저는 사망 */
		userManager.getUser(dieUser).setState("die");

		/* 직업별 유저 수 갱신 */
		String dieuserCharacter = userManager.getUser(dieUser).getCharacter();
		int num = numOfChractor.get(dieuserCharacter);
		numOfChractor.put(dieuserCharacter, num - 1);
	}

	public String isGameOver() {
		int numberOfMafia = numOfChractor.get("MAFIA");
		int numberOfCop = numOfChractor.get("COP");
		int numberOfDoctor = numOfChractor.get("DOCTOR");
		int numberOfCivil = numOfChractor.get("CIVIL");
		
		Logger.i("---------------------중간 결과 -------------------\n");
		Logger.i("마피아  " + numberOfMafia + "명 ," + "경찰 " + numberOfCop + "명, " + "의사 " + numberOfDoctor + "명 , " + "시민 " + numberOfCivil + "명 생존!!!\n");

		if (numberOfMafia == 0){
			return "MAFIALOSE";
		}

		if (numberOfMafia >= (numberOfCop + numberOfDoctor + numberOfCivil)){
			return "MAFIAWIN";
		}

		return "NOGAMEOVER";
	}

	public void gameOver() {
		state 		= "";
		when 		= "";
		wantnext 	= false;
		numOfChractor.clear(); // 직업별 인원 배정
		userManager.getUsers().clear();
	}

	public void endNight() {
		copChoice 		= "";
		doctorChoice 	= "";
		mafiaChoice.clear();
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getWhen() {
		return when;
	}

	public void setWhen(String when) {
		this.when = when;
	}

	public boolean isWantnext() {
		return wantnext;
	}

	public void setWantnext(boolean wantnext) {
		this.wantnext = wantnext;
	}

	public String getCopChoice() {
		return copChoice;
	}

	public String getDoctorChoice() {
		return doctorChoice;
	}

}
