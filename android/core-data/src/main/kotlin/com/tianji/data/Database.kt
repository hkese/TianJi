package com.tianji.data
import androidx.room.*
import com.tianji.domain.*
import kotlinx.coroutines.flow.Flow
@Entity(tableName="goals") data class GRow(@PrimaryKey val id:String, val payload:String)
@Entity(tableName="milestones", foreignKeys=[ForeignKey(entity=GRow::class,parentColumns=["id"],childColumns=["goalId"],onDelete=ForeignKey.CASCADE)], indices=[Index("goalId")]) data class MRow(@PrimaryKey val id:String, val goalId:String, val payload:String)
@Entity(tableName="charts") data class CRow(@PrimaryKey val id:String, val payload:String)
@Entity(tableName="plans", foreignKeys=[ForeignKey(entity=GRow::class,parentColumns=["id"],childColumns=["goalId"],onDelete=ForeignKey.CASCADE)], indices=[Index("goalId")]) data class PRow(@PrimaryKey val id:String, val goalId:String, val payload:String)
@Entity(tableName="actions", foreignKeys=[ForeignKey(entity=PRow::class,parentColumns=["id"],childColumns=["planVersionId"],onDelete=ForeignKey.CASCADE),ForeignKey(entity=CRow::class,parentColumns=["id"],childColumns=["chartId"],onDelete=ForeignKey.CASCADE)], indices=[Index("planVersionId"),Index("chartId")]) data class ARow(@PrimaryKey val id:String, val planVersionId:String, val chartId:String, val payload:String)
@Entity(tableName="journal", foreignKeys=[ForeignKey(entity=ARow::class,parentColumns=["id"],childColumns=["actionId"],onDelete=ForeignKey.CASCADE)], indices=[Index("actionId")]) data class JRow(@PrimaryKey val id:String, val actionId:String, val payload:String)
@Entity(tableName="consents") data class ConsentRow(@PrimaryKey val id:String,val scope:String,val grantedAt:String,val revokedAt:String?)
@Entity(tableName="providers") data class ProviderRow(@PrimaryKey val id:String,val baseUrl:String,val modelName:String,val credentialAlias:String)
@Dao interface TianJiDao {
@Query("SELECT * FROM goals") suspend fun goals():List<GRow>
@Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun put(row:GRow)
@Query("DELETE FROM goals") suspend fun cleargoals()
@Query("SELECT * FROM milestones") suspend fun milestones():List<MRow>
@Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun put(row:MRow)
@Query("DELETE FROM milestones") suspend fun clearmilestones()
@Query("SELECT * FROM charts") suspend fun charts():List<CRow>
@Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun put(row:CRow)
@Query("DELETE FROM charts") suspend fun clearcharts()
@Query("SELECT * FROM plans") suspend fun plans():List<PRow>
@Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun put(row:PRow)
@Query("DELETE FROM plans") suspend fun clearplans()
@Query("SELECT * FROM actions") suspend fun actions():List<ARow>
@Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun put(row:ARow)
@Query("DELETE FROM actions") suspend fun clearactions()
@Query("SELECT * FROM journal") suspend fun journal():List<JRow>
@Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun put(row:JRow)
@Query("DELETE FROM journal") suspend fun clearjournal()
@Query("DELETE FROM goals WHERE id=:id") suspend fun deleteGoal(id:String)
@Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun consent(row:ConsentRow)
@Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun provider(row:ProviderRow)
@Query("SELECT * FROM providers WHERE id='provider'") suspend fun provider():ProviderRow?
@Query("DELETE FROM providers") suspend fun clearProviders()
@Query("DELETE FROM consents") suspend fun clearConsents()
}
@Database(entities=[GRow::class,MRow::class,CRow::class,PRow::class,ARow::class,JRow::class,ConsentRow::class,ProviderRow::class],version=2,exportSchema=true)
abstract class TianJiDatabase:RoomDatabase(){ abstract fun dao():TianJiDao }
