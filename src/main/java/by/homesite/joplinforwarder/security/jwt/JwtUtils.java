package by.homesite.joplinforwarder.security.jwt;

import java.util.Date;

import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import by.homesite.joplinforwarder.config.ApplicationProperties;
import by.homesite.joplinforwarder.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

import javax.crypto.SecretKey;

@Component
public class JwtUtils
{

	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	@Autowired
	private ApplicationProperties applicationProperties;

	public String generateJwtToken(Authentication authentication)
	{
		UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
		Date expDate = new Date((new Date()).getTime() + Long.parseLong(applicationProperties.getGeneral().getJwtExpirationMs()));
		return Jwts.builder().setSubject((userPrincipal.getUsername())).setIssuedAt(new Date())
				.setExpiration(expDate).signWith(SignatureAlgorithm.HS512, applicationProperties.getGeneral().getJwtSecret())
				.compact();
	}

	public String getUserNameFromJwtToken(String token)
	{
		SecretKey secret = Keys.hmacShaKeyFor(applicationProperties.getGeneral().getJwtSecret().getBytes());
		return Jwts.parser().verifyWith(secret).build().parseSignedClaims(token).getPayload().getSubject();
	}

	public boolean validateJwtToken(String authToken)
	{
		try
		{
			SecretKey secret = Keys.hmacShaKeyFor(applicationProperties.getGeneral().getJwtSecret().getBytes());
			Jwts.parser().verifyWith(secret).build().parseSignedClaims(authToken);
			return true;
		}
		catch (SignatureException e)
		{
			logger.error("Invalid JWT signature: {}", e.getMessage());
		}
		catch (MalformedJwtException e)
		{
			logger.error("Invalid JWT token: {}", e.getMessage());
		}
		catch (ExpiredJwtException e)
		{
			logger.error("JWT token is expired: {}", e.getMessage());
		}
		catch (UnsupportedJwtException e)
		{
			logger.error("JWT token is unsupported: {}", e.getMessage());
		}
		catch (IllegalArgumentException e)
		{
			logger.error("JWT claims string is empty: {}", e.getMessage());
		}
		return false;
	}
}
