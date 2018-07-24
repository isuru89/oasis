import React, { Component } from 'react'
import styled from 'styled-components';
import CircularProgressbar from 'react-circular-progressbar';
import { formatInt } from "../../utils";

const Wrapper = styled.div`
  padding: 10px;
  display: flex;
  justify-content: space-evenly;
`

const Table = styled.table`
  width: 100%;
  border: 1px;
`

const ColumnLevel = styled.td`
  text-align: center;
`

const MilestoneWrapper = styled.div`
  text-align: center;
  width: 100%;
`

const MilestoneLabel = styled.div`
  padding: 0 0 10px 0;
`

const LevelNumber = styled.div`
  font-size: 2em;
  line-height: 1;
`

const LevelLabel = styled.div`
  line-height: 1;
  font-weight: lighter;
  text-transform: uppercase;
  font-size: 12px;
`

const ProgressContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
`

const BottomLabel = styled.div`
  font-weight: lighter;
  font-size: 12px;
  padding-top: 10px;
`

class CustomContentProgressBar extends Component {
  render() {
    const { children, ...otherProps } = this.props;

    return (
      <div
        style={{
          position: 'relative',
          width: '100px',
          height: '100px',
        }}
      >
        <div style={{ position: 'absolute' }}>
          <CircularProgressbar {...otherProps} />
        </div>
        <div
          style={{
            position: 'absolute',
            height: '100%',
            width: '100%',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            alignItems: 'center',
          }}
        >
          {this.props.children}
        </div>
      </div>
    );
  }
}

export default class MilestoneView extends Component {
  render() {
    const { data } = this.props;

    return (
      <Wrapper>
        {
          data.map(r => {
            return (
              <MilestoneWrapper>
                <MilestoneLabel>{r.milestone}</MilestoneLabel>
                <ProgressContainer>
                  <CustomContentProgressBar percentage={r.progress}>
                    <LevelNumber>{r.level}</LevelNumber>
                    <LevelLabel>Level</LevelLabel>
                  </CustomContentProgressBar>
                </ProgressContainer>
                {
                  r.remaining && r.remaining > 0 &&
                  <BottomLabel>
                    <div style={{ fontWeight: 'normal', fontSize: 11 }}>
                      <span style={{ paddingRight: 2, letterSpacing: 0.4, fontSize: 16 }}>{formatInt(r.nextLevel - r.remaining)}</span>
                      <span>/{formatInt(r.nextLevel)}</span>
                    </div>
                    {" "} for next level 
                  </BottomLabel>
                }
              </MilestoneWrapper>
            )
          })
        }
      </Wrapper>
    )
  }
}
