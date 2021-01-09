package OOP.Solution;

@OOPTestClass(value= OOPTestClass.OOPTestClassType.ORDERED)
public class A2 extends A1{
    @OOPTest(order=3, tag = "KKK")
    void getIt2() {
        System.out.println("This is getIt2 in A2");
    }
    @OOPBefore(value={"getThat1", "getIt2"})
    void getThat2() {
        System.out.println("This is getThat2 in A2");
    }
}