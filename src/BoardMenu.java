import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BoardMenu {
    BoardDao boardDao = new BoardDao();
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    public void mainmenu()
    {
        boardDao.list();
        System.out.println("메인 메뉴: 1.Create | 2.Read | 3.Clear | 4.Exit");
        System.out.println("메뉴 선택:");
        int menuno = 0;
        try {
            menuno = Integer.parseInt(br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) // parseInt 제대로 안될경우 예외처리
        {
            System.out.println("잘못 입력하셨습니다.");
        }
        switch (menuno)
        {
            case 1 :
                boardDao.create();
                mainmenu();
                break;
            case 2:
                boardDao.read();
                mainmenu();
                break;
            case 3:
                boardDao.clear();
                mainmenu();
                break;
            case 4:
                boardDao.exit();
                break;
            default:
                System.out.println("잘못 입력하셨습니다.");
                mainmenu();
        }
    }
}
