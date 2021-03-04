package alexlushiku.faka;

import android.content.Intent;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import alexlushiku.faka.R;

public class DescriptionsAdapter extends RecyclerView.Adapter<DescriptionsAdapter.ViewHolder> {

    private Word word;

    public DescriptionsAdapter(Word word) {
        this.word = word;
    }

    public class ViewHolder extends  RecyclerView.ViewHolder {

        public RelativeLayout layout;
        public TextView title;
        public TextView description;
        public TextView example;

        public ViewHolder(View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.desc);
            title = itemView.findViewById(R.id.word_title);
            description = itemView.findViewById(R.id.word_meaning);
            example = itemView.findViewById(R.id.word_example);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.word_description, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.title.setText(word.getWord());
        holder.description.setText(word.getMeaning(position));
        holder.example.setText(word.getExample(position));
    }

    @Override
    public int getItemCount() {
        return word.getSize();
    }

}
