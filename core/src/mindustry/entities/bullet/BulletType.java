package mindustry.entities.bullet;

import arc.audio.*;
import arc.math.*;
import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.effect.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;

public abstract class BulletType extends Content{
    public float lifetime;
    public float speed;
    public float damage;
    public float hitSize = 4;
    public float drawSize = 40f;
    public float drag = 0f;
    public boolean pierce;
    public Effect hitEffect, despawnEffect;

    /** Effect created when shooting. */
    public Effect shootEffect = Fx.shootSmall;
    /** Extra smoke effect created when shooting. */
    public Effect smokeEffect = Fx.shootSmallSmoke;
    /** Sound made when hitting something or getting removed.*/
    public Sound hitSound = Sounds.none;
    /** Extra inaccuracy when firing. */
    public float inaccuracy = 0f;
    /** How many bullets get created per ammo item/liquid. */
    public float ammoMultiplier = 2f;
    /** Multiplied by turret reload speed to get final shoot speed. */
    public float reloadMultiplier = 1f;
    /** Recoil from shooter entities. */
    public float recoil;
    /** Whether to kill the shooter when this is shot. For suicide bombers. */
    public boolean killShooter;
    /** Whether to instantly make the bullet disappear. */
    public boolean instantDisappear;
    /** Damage dealt in splash. 0 to disable.*/
    public float splashDamage = 0f;
    /** Knockback in velocity. */
    public float knockback;
    /** Whether this bullet hits tiles. */
    public boolean hitTiles = true;
    /** Status effect applied on hit. */
    public StatusEffect status = StatusEffects.none;
    /** Intensity of applied status effect in terms of duration. */
    public float statusDuration = 60 * 10f;
    /** Whether this bullet type collides with tiles. */
    public boolean collidesTiles = true;
    /** Whether this bullet type collides with tiles that are of the same team. */
    public boolean collidesTeam = false;
    /** Whether this bullet type collides with air units. */
    public boolean collidesAir = true;
    /** Whether this bullet types collides with anything at all. */
    public boolean collides = true;
    /** Whether velocity is inherited from the shooter. */
    public boolean keepVelocity = true;

    //additional effects

    public int fragBullets = 9;
    public float fragVelocityMin = 0.2f, fragVelocityMax = 1f;
    public BulletType fragBullet = null;

    /** Use a negative value to disable splash damage. */
    public float splashDamageRadius = -1f;

    public int incendAmount = 0;
    public float incendSpread = 8f;
    public float incendChance = 1f;

    public float homingPower = 0f;
    public float homingRange = 50f;

    public int lightining;
    public int lightningLength = 5;

    public float hitShake = 0f;

    public BulletType(float speed, float damage){
        this.speed = speed;
        this.damage = damage;
        lifetime = 40f;
        hitEffect = Fx.hitBulletSmall;
        despawnEffect = Fx.hitBulletSmall;
    }

    /** Returns maximum distance the bullet this bullet type has can travel. */
    public float range(){
        return speed * lifetime * (1f - drag);
    }

    public boolean collides(Bulletc bullet, Tile tile){
        return true;
    }

    public void hitTile(Bulletc b, Tile tile){
        hit(b);
    }

    public void hit(Bulletc b){
        hit(b, b.getX(), b.getY());
    }

    public void hit(Bulletc b, float x, float y){
        hitEffect.at(x, y, b.getRotation());
        hitSound.at(b);

        Effects.shake(hitShake, hitShake, b);

        if(fragBullet != null){
            for(int i = 0; i < fragBullets; i++){
                float len = Mathf.random(1f, 7f);
                float a = Mathf.random(360f);
                fragBullet.create(b, x + Angles.trnsx(a, len), y + Angles.trnsy(a, len), a, Mathf.random(fragVelocityMin, fragVelocityMax));
            }
        }

        if(Mathf.chance(incendChance)){
            Damage.createIncend(x, y, incendSpread, incendAmount);
        }

        if(splashDamageRadius > 0){
            Damage.damage(b.team(), x, y, splashDamageRadius, splashDamage * b.damageMultiplier());
        }
    }

    public void despawned(Bulletc b){
        despawnEffect.at(b.getX(), b.getY(), b.getRotation());
        hitSound.at(b);

        if(fragBullet != null || splashDamageRadius > 0){
            hit(b);
        }

        for(int i = 0; i < lightining; i++){
            Lightning.createLighting(Lightning.nextSeed(), b.team(), Pal.surge, damage, b.getX(), b.getY(), Mathf.random(360f), lightningLength);
        }
    }

    public void draw(Bulletc b){
    }

    public void init(Bulletc b){
        if(killShooter && b.getOwner() instanceof Healthc){
            ((Healthc)b.getOwner()).kill();
        }

        if(instantDisappear){
            b.setTime(lifetime);
        }
    }

    public void update(Bulletc b){
        if(homingPower > 0.0001f){
            Teamc target = Units.closestTarget(b.team(), b.getX(), b.getY(), homingRange, e -> !e.isFlying() || collidesAir);
            if(target != null){
                b.vel().setAngle(Mathf.slerpDelta(b.getRotation(), b.angleTo(target), 0.08f));
            }
        }
    }

    @Override
    public ContentType getContentType(){
        return ContentType.bullet;
    }

    //TODO change 'create' to 'at'

    public Bulletc create(Teamc owner, float x, float y, float angle){
        return create(owner, owner.team(), x, y, angle);
    }

    public Bulletc create(Entityc owner, Team team, float x, float y, float angle){
        return create(owner, team, x, y, angle, 1f);
    }

    public Bulletc create(Entityc owner, Team team, float x, float y, float angle, float velocityScl){
        return create(owner, team, x, y, angle, velocityScl, 1f, null);
    }

    public Bulletc create(Entityc owner, Team team, float x, float y, float angle, float velocityScl, float lifetimeScl){
        return create(owner, team, x, y, angle, velocityScl, lifetimeScl, null);
    }

    public Bulletc create(Bulletc parent, float x, float y, float angle){
        return create(parent.getOwner(), parent.team(), x, y, angle);
    }

    public Bulletc create(Bulletc parent, float x, float y, float angle, float velocityScl){
        return create(parent.getOwner(), parent.team(), x, y, angle, velocityScl);
    }

    public Bulletc create(Entityc owner, Team team, float x, float y, float angle, float velocityScl, float lifetimeScl, Object data){


        //TODO implement
        return null;
        /*
        Bullet bullet = Pools.obtain(Bullet.class, Bullet::new);
        bullet.type = type;
        bullet.owner = owner;
        bullet.data = data;

        bullet.velocity.set(0, type.speed).setAngle(angle).scl(velocityScl);
        if(type.keepVelocity){
            bullet.velocity.add(owner instanceof VelocityTrait ? ((VelocityTrait)owner).velocity() : Vec2.ZERO);
        }

        bullet.team = team;
        bullet.type = type;
        bullet.lifeScl = lifetimeScl;

        bullet.set(x - bullet.velocity.x * Time.delta(), y - bullet.velocity.y * Time.delta());
        bullet.add();

        return bullet;*/
    }

    public void createNet(Team team, float x, float y, float angle, float velocityScl, float lifetimeScl){
        Call.createBullet(this, team, x, y, angle, velocityScl, lifetimeScl);
    }

    @Remote(called = Loc.server, unreliable = true)
    public static void createBullet(BulletType type, Team team, float x, float y, float angle, float velocityScl, float lifetimeScl){
        type.create(null, team, x, y, angle, velocityScl, lifetimeScl, null);
    }
}
