import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.*
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*


class App(private val files: Array<File>, private var index: Int = 0) : JFrame() {
    private val panel = JPanel(BorderLayout())
    private lateinit var myPanel: MyPanel

    init {
        layout = BorderLayout()
        add(panel, BorderLayout.CENTER)
        val im: InputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        val am = panel.actionMap
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "entered")
        am.put("entered", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                forwardPage()
            }
        })
        files.sort()
        loadPage()
    }

    private fun loadPage() {
        if (index >= files.size) {
            JOptionPane.showMessageDialog(this, "", "There's more page", JOptionPane.PLAIN_MESSAGE)
            return
        }
        println("loading page $index")
        myPanel = MyPanel(files[index]);
        panel.add(myPanel, BorderLayout.CENTER)
        title = "page $index"
    }

    private fun forwardPage() {
        myPanel.sliceImage()
        panel.remove(myPanel)
        index += 1
        loadPage()
        panel.isVisible = false
        panel.isVisible = true
    }

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val files = File(args[0]).listFiles()!!
            val index = if (args.size > 1) {
                Integer.parseInt(args[1])
            } else {
                0
            }
            SwingUtilities.invokeLater {
                val app = App(files, index)
                app.isVisible = true
                app.size = Dimension(1000, 1000)
                app.defaultCloseOperation = EXIT_ON_CLOSE
                app.extendedState = JFrame.MAXIMIZED_BOTH
            }
        }
    }
}

class MyPanel(private val file: File) : JPanel() {
    private val image = ImageIO.read(file)
    private val points = ArrayList<Int>()

    init {
        points.add(0)
        layout = BorderLayout()
        preferredSize = Dimension(image.width, image.height)
        addMouseListener(MyMouseListener(points, this))
    }

    fun sliceImage() {
        File("slices").mkdirs()
        points.removeIf { p -> p > image.height }
        points.sort()
        val img = ImageIO.read(file)
        for (i in 0 until points.size) {
            val y = points[i]
            val h = if (i == points.size - 1) {
                img.height - y
            } else {
                points[i + 1] - y
            }
            val sub = image.getSubimage(0, y, image.width, h);
            ImageIO.write(sub, "jpg", File("slices/${file.nameWithoutExtension}" + "_%04d.jpg".format(i)));
        }
    }

    override fun paint(g: Graphics) {
        super.paintComponent(g)
        g.drawImage(image, 0, 0, null)
        for (p in points) {
            g.drawLine(0, p, image.width, p);
        }
    }
}

class MyMouseListener(private val points: ArrayList<Int>, private val panel: JPanel) : MouseListener {
    override fun mouseClicked(p0: MouseEvent?) {
    }

    override fun mousePressed(p0: MouseEvent?) {
        println(p0?.y)
        points.add(p0!!.y)
        panel.repaint()
    }

    override fun mouseReleased(p0: MouseEvent?) {
    }

    override fun mouseEntered(p0: MouseEvent?) {
    }

    override fun mouseExited(p0: MouseEvent?) {
    }
}

