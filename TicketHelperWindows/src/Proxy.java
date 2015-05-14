
public class Proxy implements Comparable {
	String ip;
	int port;
	boolean located;
	long rrt;
	int weight;
	public long getRrt() {
		return rrt;
	}
	public void setRrt(long rrt) {
		this.rrt = rrt;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public boolean isLocated() {
		return located;
	}
	public void setLocated(boolean located) {
		this.located = located;
	}
	@Override
	public int compareTo(Object arg0) {
		if(arg0 instanceof Proxy){
			int thisTime=getWeight();
			int tarTime=((Proxy) arg0).getWeight();
			return thisTime<tarTime?1:(thisTime>tarTime?-1:0);
			//return thisRrt<tarRrt?1:(thisRrt>tarRrt?-1:0);
		}
		return 0;
	}
	public int getWeight(){
		return weight;
	}
	public boolean equals(Proxy p){
		if(p==null){
			return false;
		}
		if(this.ip.equals(p.getIp())){
			return true;
		}
		return false;
	}
	public void insWeight(int time){
		weight+=time;
	}
	public void desWeight(){
		weight-=10;
	}
}
