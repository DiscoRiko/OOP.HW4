package OOP.Solution;

public class AClonable implements Cloneable{
    public AClonable() {}
    public AClonable clone() throws CloneNotSupportedException {
        System.out.println("AClonable clone");
        return (AClonable)super.clone();
    }
}
