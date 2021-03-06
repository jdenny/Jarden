package jarden.cardapp;

import jarden.cards.Card;
import jarden.cards.CardPack;
import jarden.cards.Hand;
import jarden.cards.Player;
import jarden.cards.Suit;

import java.util.ArrayList;

import com.jardenconsulting.cardapp.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PlayerFragment extends Fragment {
	private View view;
	private Player player;
	private CardPack cardPack;
	private TextView playerTitle;
	private TextView spadeValues;
	private TextView heartValues;
	private TextView diamondValues;
	private TextView clubValues;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.player_view, container, false);

		playerTitle = (TextView) view.findViewById(R.id.playerTitle);
		spadeValues = (TextView) view.findViewById(R.id.spades);
		heartValues = (TextView) view.findViewById(R.id.hearts);
		diamondValues = (TextView) view.findViewById(R.id.diamonds);
		clubValues = (TextView) view.findViewById(R.id.clubs);

		return view;
	}
	public void setData(Player player, CardPack cardPack) {
		this.player = player;
		this.cardPack = cardPack;
	}
	public void getHand() {
		Hand hand = cardPack.getHand(player);
		this.playerTitle.setText(player.toString());
		ArrayList<Card> cards = hand.getCards();
		StringBuilder sb = new StringBuilder();
		int index = 0;
		int handSize = cards.size();
		Card card;
		while (index < handSize) {
			card = cards.get(index);
			if (!card.getSuit().equals(Suit.Club)) break;
			sb.append(card.getShortRank() + " ");
			++index;
		}
		clubValues.setText(sb.toString());
		sb.setLength(0);
		while (index < handSize) {
			card = cards.get(index);
			if (!card.getSuit().equals(Suit.Diamond)) break;
			sb.append(card.getShortRank() + " ");
			++index;
		}
		diamondValues.setText(sb.toString());
		sb.setLength(0);
		while (index < handSize) {
			card = cards.get(index);
			if (!card.getSuit().equals(Suit.Heart)) break;
			sb.append(card.getShortRank() + " ");
			++index;
		}
		heartValues.setText(sb.toString());
		sb.setLength(0);
		while (index < handSize) {
			card = cards.get(index);
			if (!card.getSuit().equals(Suit.Spade)) break;
			sb.append(card.getShortRank() + " ");
			++index;
		}
		spadeValues.setText(sb.toString());
	}

}
