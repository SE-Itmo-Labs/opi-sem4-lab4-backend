package database.model

import coordinates.geometry.Point2DR
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PostLoad
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity
@Table(name = "lab_points", indexes = [
    Index(name = "idx_user_id", columnList = "user_id")
])
open class Point2DRow(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: DotType,

    @Embedded
    var point2DR: Point2DR,

    @Column(nullable = false)
    var timestamp: LocalDateTime = LocalDateTime.now().plusHours(3),

    @Column(nullable = false)
    var executionTime: Long = 0,

    @Column(nullable = false)
    var inArea: Boolean = false,

    @Transient
    var formattedTimestamp: String =
        timestamp.format(
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
        ),
) {

//    constructor() : this(
//        id = null,
//        point2DR = Point2DR(0f, 0f, 0f),
//        timestamp = LocalDateTime.now().plusHours(3),
//        executionTime = 0,
//        inArea = false,
//        formattedTimestamp = "",
//        user = User(1, "", ""),
//        type = DotType.SIMPLE
//    )

    constructor() : this(
        user = User(),
        type = DotType.SIMPLE,
        point2DR = Point2DR()
    )

    @PostLoad
    @PrePersist
    open fun updateFormattedTimestamp() {
        this.formattedTimestamp = this.timestamp.format(
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        )
    }
}