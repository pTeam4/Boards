import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class BoardDao {
    PreparedStatement pstmt;
    BufferedReader br;
    Connection conn;

    public BoardDao() {
        this.conn = JdbcConnection.getInstance().getConnection();
        this.br = new BufferedReader(new InputStreamReader(System.in));
    }

    public void create() {
        //
        String sql = "Insert into board(btitle, bcontent,bwriter,bdate, bfilename, bfiledata)"
                + "values (?,?,?,now(),?, ?)";

        try (
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ) {

            System.out.println("[새 게시물 입력]");
            System.out.println("제목: ");
            String btitle = br.readLine();

            System.out.println("글 내용을 입력하세요. 다 작성하셨으면 exit를 입력해주세요.");
            StringBuilder sb = new StringBuilder();
            String line;

            while (!(line = br.readLine()).equals("exit")) {
                sb.append(line).append("\r\n"); // 각 줄의 끝에 \r\n 추가
            }

            sb.delete(sb.length() - 2, sb.length()); //맨마지막 개행 삭제
            String bcontent = sb.toString();

            System.out.print("\n작성자: ");
            String bwriter = br.readLine();

            System.out.print("\n파일이름: ");
            String bfilename = br.readLine();

            System.out.print("\n파일경로: ");
            String bfiledata = br.readLine();

            pstmt.setString(1, btitle);
            pstmt.setString(2, bcontent);
            pstmt.setString(3, bwriter);
            pstmt.setString(4, bfilename);
            pstmt.setBlob(5, new FileInputStream(bfiledata));

            pstmt.executeUpdate();
        } catch (FileNotFoundException e) {
            System.out.println("해당 파일이 존재하지 않습니다.");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * BoardMain -> BoardMenu -> case2
     * bno(게시글번호)를 받아 db 에서 해당 튜플(게시글)을 출력합니다.
     * 튜플 출력 후 해당 튜플에 대한 보조 메뉴를 출력합니다.
     * @author 이맑음
     */
    public void read() {
        list();
        //읽어올 게시글 번호를 저장할 객체
        int bno;
        //db에서 읽어온 값들을 저장해줄 객체 생성 및 초기화
        Board board = new Board();
        try {
            System.out.print("읽어올 게시물 번호를 입력해주세요 : ");
            bno = Integer.parseInt(br.readLine());
            //매개변수화 된 sql 문 작성
            String sql = "select * from board where bno = ?";
            //preparedstatment 얻기 및 값 지정
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bno);
            //sql문 실행
            ResultSet rs = pstmt.executeQuery();
            //db에서 읽어온 값들을 board 객체에 할당
            if (rs.next()) {
                board.setBno(rs.getInt("bno"));
                board.setBtitle(rs.getString("btitle"));
                board.setBcontent(rs.getString("bcontent"));
                board.setBwriter(rs.getString("bwriter"));
                board.setBdate(rs.getDate("bdate"));
                board.setBfilename(rs.getString("bfilename"));
                board.setBfiledata(rs.getBlob("bfiledata"));
            }
            //값 출력
            System.out.printf("게시글 번호 : %d\n", board.getBno());
            System.out.printf("제목 : %s\n", board.getBtitle());
            System.out.printf("작성자 : %s\n", board.getBwriter());
            System.out.printf("내용 : %s\n", board.getBcontent());
            System.out.printf("파일 이름 : %s\n", board.getBfilename());
            System.out.printf("첨부 파일 : %s\n", board.getBfiledata());
            //보조메뉴 출력
            printSubMenu(board);
            //사용한 자원 닫기
            rs.close();
            pstmt.close();
        } catch (IOException | SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * BoardMain -> BoardMenu -> case2 -> read()
     * read()에서 출력한 튜플(게시글)에 대한 수정/삭제를 하거나 메인메뉴로 되돌아갈 수 있습니다.
     * @param board
     * @author 이맑음
     */
    private void printSubMenu(Board board) {
        //보조메뉴에 대한 응답을 저장할 객체
        int response = 0;
        //보조메뉴 출력
        System.out.println("1.Update | 2.Delete | 3.List");
        try {
            response = Integer.parseInt(br.readLine());
            switch (response) {
                case 1 -> update(board);
                case 2 -> delete(board);
                case 3 -> list();
                default -> {
                    System.out.println("잘못된 입력입니다. 1.다시 돌아가기 | 2.메인 메뉴로 돌아가기");
                    int response2 = Integer.parseInt(br.readLine());
                    switch (response2) {
                        case 1 -> printSubMenu(board);
                        default -> System.out.println("메인 메뉴로 돌아갑니다.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void list() {
        System.out.println("[게시물 목록]");
        System.out.println("----------------------------------------------------------------------------------------");
        System.out.printf("%-6s%-12s%-10s%-25s%-16s%-16s%n", "no", "title", "writer", "content", "file name3", "date");
        System.out.println("----------------------------------------------------------------------------------------");

        String sql = "SELECT * FROM board";

        try (
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();
        ) {

            while (rs.next()) {
                String content = rs.getString("bcontent").replace("\r\n", " ");
                if(content.length() > 10)
                {
                    content = content.substring(0, 10) + "...";
                }

                System.out.printf(
                        "%-6s%-12s%-10s%-25s%-16s%-16s%n",
                        rs.getInt("bno"),
                        rs.getString("btitle"),
                        rs.getString("bwriter"),
                        content,
                        rs.getString("bfilename"),
                        rs.getDate("bdate")
                );
            }

            System.out.println(
                    "----------------------------------------------------------------------------------------\n"
            );

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(Board board) {
        try {
            System.out.println(board.getBno() + " 번 게시물을 지우시겠습니까? [ Y | N ]");
            String answer = br.readLine();
            if (answer.equalsIgnoreCase("Y")) {
                String sql = new StringBuilder().append("DELETE FROM board WHERE bno=?").toString();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, board.getBno());
                pstmt.executeUpdate();
                System.out.println("게시글이 삭제되었습니다.");
            } else if (answer.equalsIgnoreCase("N")) {
                System.out.println("메뉴로 돌아갑니다.");
            } else {
                System.out.println("Y 또는 N을 입력하세요");
            }
            pstmt.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void update(Board board) {
        try {
            int bno = board.getBno();
            System.out.println("제목을 수정하시겠습니까? Y/N");
            String yesorno = br.readLine();
            if (yesorno.equals("Y")) {
                System.out.println("수정할 글 제목을 입력하세요.");
                String updatetitle = br.readLine();
                String sql = "UPDATE board SET " +
                        "btitle = ? " +
                        "where bno = ?";
                //PreparedStatement 얻기 및 값 지정
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, updatetitle);
                pstmt.setInt(2, bno);
                pstmt.executeUpdate();
                System.out.println("글 제목 수정 완료 되었습니다.");
            }
            System.out.println("글 내용을 수정하시겠습니까? Y/N");
            yesorno = br.readLine();
            if (yesorno.equals("Y")) {
                System.out.println("수정한 글 내용을 입력하세요. 다 작성하셨으면 exit를 입력해주세요.");
                StringBuilder sb = new StringBuilder();
                String line;
                while (!(line = br.readLine()).equals("exit")) {
                    sb.append(line).append("\r\n"); // 각 줄의 끝에 \r\n 추가
                }
                sb.delete(sb.length()-2, sb.length()); //맨마지막 개행 삭제
                String updatecontents = sb.toString();
                String sql = "UPDATE board SET " +
                        "bcontent = ? " +
                        "where bno = ?";
                //PreparedStatement 얻기 및 값 지정
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, updatecontents);
                pstmt.setInt(2, bno);
                pstmt.executeUpdate();
                System.out.println("글내용 수정 완료 되었습니다.");
            }
            System.out.println("첨부파일을 수정하시겠습니까? Y/N");
            yesorno = br.readLine();
            if (yesorno.equals("Y")) {
                System.out.println("수정할 첨부파일 명을 입력해주세요.");
                String updatebfilename = br.readLine();
                System.out.println("수정할 첨부파일 경로를 입력해주세요.");
                String updatebfilepath = br.readLine();
                String sql = "UPDATE board SET " +
                        "bfilename = ?, bfiledata = ? " +
                        "where bno = ?";
                //PreparedStatement 얻기 및 값 지정
                pstmt = conn.prepareStatement(sql);
                Boolean flag = false;
                try
                {
                    pstmt.setBlob(2, new FileInputStream(updatebfilepath));
                }
                catch (IOException e)
                {
                    flag = true;
                    System.out.println("파일 경로 오류입니다. 파일경로를 확인해주세요.");
                }
                if(!flag)
                {
                    pstmt.setString(1, updatebfilename);
                    pstmt.setInt(3, bno);
                    pstmt.executeUpdate();
                    System.out.println("첨부파일 수정 완료 되었습니다.");
                }

            }
            pstmt.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void clear() {
        String sql = "DELETE FROM board";

        try (
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            System.out.println("게시물 목록을 비우시겠습니까? [ Y | N ]");
            String answer = br.readLine();

            if (answer.equalsIgnoreCase("Y")) {
                int rows = pstmt.executeUpdate();
                System.out.println("삭제된 게시물의 수 : "+ rows);
            } else if (answer.equalsIgnoreCase("N")) {
                System.out.println("메뉴로 돌아갑니다.");
            } else {
                System.out.println("Y 또는 N을 입력하세요!");
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void exit() {
        System.out.println("** 게시판 종료 **");
    }
}
