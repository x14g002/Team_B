import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Test01
 */
@WebServlet("/Test01")
public class Test01 extends HttpServlet {
	private static final String TITLE = "BBS";
	private static final long serialVersionUID = 1L;
	private Oracle mOracle;

	// タグの無効化
	public static String CONVERT(String str) {
		return str.replaceAll("&", "&amp;").replaceAll(">", "&gt;")
				.replaceAll("<", "&lt;").replaceAll("\n", "<br>");
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Test01() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init() throws ServletException {
		// TODO 自動生成されたメソッド・スタブ
		super.init();

		try {
			ServletContext context = getServletConfig().getServletContext();
			URL resource = context.getResource("/WEB-INF/db.txt");
			InputStream stream = resource.openStream();
			Scanner sc = new Scanner(stream);
			String id = sc.next();
			String pass = sc.next();
			sc.close();
			stream.close();

			mOracle = new Oracle();
			mOracle.connect("ux4", id, pass);

		} catch (Exception e) {
			System.err.println("db.txtにユーザ情報が設定されていない、もしくは認証に失敗しました");
		}
	}

	@Override
	public void destroy() {
		// DB切断
		mOracle.close();
		// TODO 自動生成されたメソッド・スタブ
		super.destroy();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		action(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		action(request, response);
	}

	protected void action(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// 要求文字コードのセット(Javaプログラムからはき出す文字コード)
		response.setCharacterEncoding("UTF-8");
		// 応答文字コードのセット(クライアントに通知する文字コードとファイルの種類)
		response.setContentType("text/html; charset=UTF-8");

		// 出力ストリームの取得
		PrintWriter out = response.getWriter();

		// パラメータにデータがあった場合はDBへ挿入
		String param1 = request.getParameter("data1");
		String param2 = request.getParameter("title");
		String param3 = request.getParameter("comment");
		String param4 = request.getParameter("k_id");
		String param5 = request.getParameter("k_delete");
		String param6 = request.getParameter("delete_id");
		String param7 = request.getParameter("g_id");
		String param8 = request.getParameter("g_name");
		String userID = "bbsteambadmin";
		String adminID = "bbsteambadmin";

		if (param1 != null && param1.length() > 0&&param2 != null && param2.length() > 0) {
			// UTF8をJava文字列に変換
			String data1 = new String(param1.getBytes("ISO-8859-1"), "UTF-8");
			String title = new String(param2.getBytes("ISO-8859-1"), "UTF-8");

			// SQL文の作成 Oracle.STRはシングルクオートのエスケープ処理
			String sql = String
					.format("insert into t_kakikomi values(BBSSEQ.nextval,'%s','%s', null,1,'%s',0)",
							Oracle.STR(title), userID, Oracle.STR(data1));
			// デバッグ用
			System.out.println("DEBUG:SQL文 " + sql);
			// DBにSQL文を実行させる
			mOracle.execute(sql);
		} else if (param3 != null && param3.length() > 0) {
			String comment = new String(param3.getBytes("ISO-8859-1"), "UTF-8");
			String k_id = new String(param4.getBytes("ISO-8859-1"), "UTF-8");

			String sql = String
					.format("insert into t_com values(BBSCOM.nextval,'%s','%s', null,'%s')",
							Oracle.STR(k_id), userID, Oracle.STR(comment));
			System.out.println("DEBUG:SQL文 " + sql);
			// DBにSQL文を実行させる
			mOracle.execute(sql);

			sql = String.format(
					"update t_kakikomi set k_com = k_com + 1 where k_id = %s",
					k_id);
			System.out.println("DEBUG:SQL文 " + sql);
			// DBにSQL文を実行させる
			mOracle.execute(sql);

			// 削除用
		} else if (param5 != null && param5.length() > 0) {
			String k_delete = new String(param5.getBytes("ISO-8859-1"), "UTF-8");
			String sql = String.format(
					"DELETE FROM t_kakikomi WHERE k_id = %s", k_delete);
			System.out.println("DEBUG:SQL文 " + sql);
			// DBにSQL文を実行させる
			mOracle.execute(sql);
			sql = String.format("DELETE FROM t_com WHERE k_id = %s", k_delete);
			System.out.println("DEBUG:SQL文 " + sql);
			mOracle.execute(sql);
			// コメント削除
		} else if (param6 != null && param6.length() > 0) {
			String delete_id = new String(param6.getBytes("ISO-8859-1"),
					"UTF-8");
			int index = delete_id.indexOf(",");
			String cid = delete_id.substring(0, index);
			String kid = delete_id.substring(index + 1);
			String sql = String
					.format("DELETE FROM t_com WHERE c_id = %s", cid);
			System.out.println("DEBUG:SQL文 " + sql);
			mOracle.execute(sql);
			sql = String.format(
					"update t_kakikomi set k_com = k_com - 1 where k_id = %s",
					kid);
			System.out.println("DEBUG:SQL文 " + sql);
			mOracle.execute(sql);

		} else if (param7 != null && param7.length() > 0 && param8 != null
				&& param8.length() > 0) {
			// UTF8をJava文字列に変換
			String g_id = new String(param7.getBytes("ISO-8859-1"), "UTF-8");
			String g_name = new String(param8.getBytes("ISO-8859-1"), "UTF-8");

			// SQL文の作成 Oracle.STRはシングルクオートのエスケープ処理
			String sql = String.format(
					"update t_genre set g_name = '%s' where g_id = %s",
					Oracle.STR(g_name), Oracle.STR(g_id));
			// デバッグ用
			System.out.println("DEBUG:SQL文 " + sql);
			// DBにSQL文を実行させる
			mOracle.execute(sql);

		}
		// 開始部分の出力
		out.format(
				"<!DOCTYPE html>\n"
						+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
						+ "<head>\n"
						+ "<meta http-equiv=\"content-style-type\" content=\"text/css\">\n"
						+ "<title>%s</title>\n"
						+ "<style type=\"text/css\">\n"
						+ "<!--\n"
						+ "#body	{\n"
						+ "	background-color: #FFF;\n"
						+ "	width: 500px;\n"
						+ "	margin: 0 auto;\n"
						+ "	border: 1px solid #aaa;\n"
						+ "	margin-top: 10px;\n"
						+ "}\n"
						+ "#header	{\n"
						+ "	background-color: #000;\n"
						+ "	height: 50px;\n"
						+ "}\n"
						+ "hr	{\n"
						+ "	border:none;\n"
						+ "   border-top:dashed 1px #aaa;\n"
						+ "  height:1px;\n"
						+ " color:#FFFFFF;\n"
						+ "  margin: 0 6 0 6;\n"
						+ " width: 470px;\n"
						+ "}\n"
						+ ".k_header	{\n"
						+ "	font-size: 16px;\n"
						+ "	padding: 10px 10px 0px 20px;\n"
						+ "}\n"
						+ ".k_mes	{\n"
						+ "	font-size: 14px;\n"
						+ "	padding: 0px 50px 10px 30px;\n"
						+ "}\n"
						+ ".k_com	{\n"
						+ "	font-size: 14px;\n"
						+ "	text-align:right;\n"
						+ "	padding: 0px 30px 0px 0px;\n"
						+ "	font-color: orange;\n"
						+ "	text-decoration:underline;\n"
						+ "}\n"
						+ ".com	{\n"
						+ "	font-size: 14px;\n"
						+ "	padding: 10px 10px 10px 50px;\n"
						+ "}\n"
						+ "#footer	{\n"
						+ "	margin-bottom: 25px;\n"
						+ "dispray: block;\n"
						+ "background-color: #000;\n"
						+ "color: #ababab;\n"
						+ "font-size: 10px;\n"
						+ "text-align: center;\n"
						+ "}\n"
						+ "a:link {\n"
						+ "	color: #ffa500; \n"
						+ "}\n"
						+ "a:visited {\n"
						+ "	color: #ffa500;\n"
						+ "}\n"
						+ "a:hover {\n"
						+ "	color: #FFFFFF; \n"
						+ "}\n"
						+ "a:active {\n"
						+ "	color: #FFFFFF; \n"
						+ "}\n"
						+ "-->\n"
						+ "</style>\n"
						+ "</head>\n"
						+ "<body>\n"
						+ "<div id=\"header\">\n"
						+ "aaaaa\n"
						+ "</div>\n"
						+ "<div id=\"body\">\n"
						+ "<br><form method=\"post\"><center>Title:<textarea name=\"title\" rows=\"1\" cols=\"30\"></textarea></center>"
						+ "<center><textarea name=\"data1\" rows=\"5\" cols=\"40\"></textarea></center>"
						+ "<br><center><input type=\"image\" img src=\"/BBS/image/button1.png\" onMouseOver=\"this .src='/BBS/image/button2.png'\" onMouseOut=\"this .src='/BBS/image/button1.png'\"  value=\"送信\"></form></center>\n",
				TITLE);

		// データの抽出
		try {
			ResultSet res = mOracle
					.query("select * from t_kakikomi ORDER BY k_id DESC");
			while (res.next()) {
				String data = res.getString(6);
				if (data != null) {
					// CONVERTはタグの無効化
					out.format("<div class=\"k_header\">■%s　　%s</div><hr>"
							+ "<div class=\"k_mes\">%s\n</div>\n"
							+ "<div class=\"k_com\">\n",
							CONVERT(res.getString(2)),
							CONVERT(res.getString(3)), CONVERT(data));
					if (userID == adminID) {
						out.format(
								"<form method=\"post\"><input type=\"image\" src=\"/BBS/image/batu01.png\" onMouseOver=\"this .src='/BBS/image/batu02.png'\" onMouseOut=\"this .src='/BBS/image/batu01.png'\" name=\"k_delete\" value=\"%s\"></form>",
								res.getString(1));
					}

					out.format(
							"<!-- 折りたたみ -->\n"
									+ "<div onclick=\"obj=document.getElementById('%s').style; obj.display=(obj.display=='none')?'block':'none';\">\n"
									+ "	<a style=\"cursor:pointer;\">コメント（%s）</a>\n"
									+ "	</div>\n" + "	<!--// 折りたたみ -->\n"
									+ "	</div>\n", res.getString(1),
							res.getString(7));

					String count = String.format(
							"SELECT COUNT(*) FROM t_com where k_id=%s",
							res.getString(1));
					ResultSet resc = mOracle.query(count);
					resc.next();
					int aa = resc.getInt(1);
					out.format(
							"<div class=\"com\">\n"
									+ "	<!-- 折りたたまれ -->\n"
									+ "	<div id=\"%s\" style=\"display:none;clear:both;\">\n",
							res.getString(1));

					if (aa == 0) {

						out.format("コメントはありません\n");

					} else {
						String s = String
								.format("select * from t_com where k_id=%s ORDER BY c_id ASC",
										res.getString(1));
						resc = mOracle.query(s);
						while (resc.next()) {
							String c = resc.getString(1);
							if (c != null) {
								out.format(" ◆ RE:　%s　　%s <br>\n"
										+ "		　　　%s<br><br>\n",
										res.getString(2), resc.getString(3),
										resc.getString(5));
								if (userID == adminID) {
									out.format(
											"<form method=\"post\"><div align=\"right\"><input type=\"image\" src=\"/BBS/image/batu01.png\" onMouseOver=\"this .src='/BBS/image/batu02.png'\" onMouseOut=\"this .src='/BBS/image/batu01.png'\" name=\"delete_id\" value=\"%s,%s\"></div></form>",
											resc.getString(1), res.getString(1));
								}
							}
						}
					}
					out.format(
							"<br><br><form method=\"post\">"
									+ "<center><textarea name=\"comment\" rows=\"5\" cols=\"40\"></textarea></center>"
									+ "<br><center><button type=\"submit\" name=\"k_id\" value=\"%s\">送信</button></form></center>\n"
									+ "</div>\n" + "	<!--// 折りたたまれ -->\n"
									+ "</div>\n", res.getString(1));
				}
			}
			out.format("<br>");
		} catch (SQLException e) {
		}

		if (userID == adminID) {
			out.format("<center><form method=\"post\">ジャンルID:<textarea name=\"g_id\" rows=\"1\" cols=\"5\"></textarea>"
					+ "　名前:<textarea name=\"g_name\" rows=\"1\" cols=\"10\"></textarea>"
					+ "<br><input type=\"image\" img src=\"/BBS/image/button1.png\" onMouseOver=\"this .src='/BBS/image/button2.png'\" onMouseOut=\"this .src='/BBS/image/button1.png'\" value=\"変更\"></form></center><br>");
		}

		// 終了部分
		out.format("</div><br><div id=\"footer\">作成:Bチーム　素材:<a href=\"http://neo-himeism.net/\" target=\"_blank\" title=\"NEO HIMEISM\">NEO HIMEISM</a></div></body>\n</html>\n");
		// 出力終了
		out.close();

	}
}
