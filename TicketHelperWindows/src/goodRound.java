
public class goodRound {
	public int id;
	public int count;
	public boolean bought;
	public String team;
	public goodRound(int id, int count,String team){
		this.id=id;
		this.count=count;
		this.team=team;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public boolean isBought() {
		return bought;
	}
	public void setBought(boolean bought) {
		this.bought = bought;
	}
	public String getTeam() {
		return team;
	}
	public void setTeam(String team) {
		this.team = team;
	}
}
